package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Renders the sky horizon. Vanilla Minecraft simply uses the "clear color" for its horizon, and then draws a plane
 * above the player. This class extends the sky rendering so that an octagonal prism is drawn around the player instead,
 * allowing shaders to perform more advanced sky rendering.
 * <p>
 * However, the horizon rendering is designed so that when sky shaders are not being used, it looks almost exactly the
 * same as vanilla sky rendering, except a few almost entirely imperceptible differences where the walls
 * of the octagonal prism intersect the top plane.
 */
public class HorizonRenderer {
	/**
	 * The Y coordinate of the top skybox plane. Acts as the upper bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float TOP = 16.0F;

	/**
	 * The Y coordinate of the bottom skybox plane. Acts as the lower bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float BOTTOM = -16.0F;

	/**
	 * Cosine of 22.5 degrees.
	 */
	private static final float COS_22_5 = (float) Math.cos(Math.toRadians(22.5));

	/**
	 * Sine of 22.5 degrees.
	 */
	private static final float SIN_22_5 = (float) Math.sin(Math.toRadians(22.5));
	private GpuBuffer buffer;
	private int currentRenderDistance;

	private int indexCount = -1;

	public HorizonRenderer() {
		currentRenderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();

		rebuildBuffer();
	}

	private void rebuildBuffer() {
		if (this.buffer != null) {
			this.buffer.close();
		}

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		// Build the horizon quads into a buffer
		buildHorizon(currentRenderDistance * 16, buffer);
		MeshData meshData = buffer.build();

		this.buffer = RenderSystem.getDevice().createBuffer(() -> "Horizon", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, meshData.vertexBuffer());
		this.indexCount = meshData.drawState().indexCount();
		meshData.close();
		Tesselator.getInstance().clear();
	}

	private void buildQuad(VertexConsumer consumer, float x1, float z1, float x2, float z2) {
		consumer.addVertex(x1, BOTTOM, z1);
		consumer.addVertex(x1, TOP, z1);
		consumer.addVertex(x2, TOP, z2);
		consumer.addVertex(x2, BOTTOM, z2);
	}

	private void buildHalf(VertexConsumer consumer, float adjacent, float opposite, boolean invert) {
		if (invert) {
			adjacent = -adjacent;
			opposite = -opposite;
		}

		// NB: Make sure that these vertices are being specified in counterclockwise order!
		// Otherwise back face culling will remove your quads, and you'll be wondering why there's a hole in your horizon.
		// Don't poke holes in the horizon. Specify vertices in counterclockwise order.

		// +X,-Z face
		buildQuad(consumer, adjacent, -opposite, opposite, -adjacent);
		// +X face
		buildQuad(consumer, adjacent, opposite, adjacent, -opposite);
		// +X,+Z face
		buildQuad(consumer, opposite, adjacent, adjacent, opposite);
		// +Z face
		buildQuad(consumer, -opposite, adjacent, opposite, adjacent);
	}

	/**
	 * @param adjacent the adjacent side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 * @param opposite the opposite side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 */
	private void buildOctagonalPrism(VertexConsumer consumer, float adjacent, float opposite) {
		buildHalf(consumer, adjacent, opposite, false);
		buildHalf(consumer, adjacent, opposite, true);
	}

	private void buildRegularOctagonalPrism(VertexConsumer consumer, float radius) {
		buildOctagonalPrism(consumer, radius * COS_22_5, radius * SIN_22_5);
	}

	private void buildBottomPlane(VertexConsumer consumer, int radius) {
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.addVertex(x + 64, BOTTOM, z);
				consumer.addVertex(x, BOTTOM, z);
				consumer.addVertex(x, BOTTOM, z + 64);
				consumer.addVertex(x + 64, BOTTOM, z + 64);
			}
		}
	}

	private void buildTopPlane(VertexConsumer consumer, int radius) {
		// You might be tempted to try to combine this with buildBottomPlane to avoid code duplication,
		// but that won't work since the winding order has to be reversed or else one of the planes will be
		// discarded by back face culling.
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.addVertex(x + 64, TOP, z);
				consumer.addVertex(x + 64, TOP, z + 64);
				consumer.addVertex(x, TOP, z + 64);
				consumer.addVertex(x, TOP, z);
			}
		}
	}

	private void buildHorizon(int radius, VertexConsumer consumer) {
		if (radius > 256) {
			// Prevent the prism from getting too large, this causes issues on some shader packs that modify the vanilla
			// sky if we don't do this.
			radius = 256;
		}

		buildRegularOctagonalPrism(consumer, radius);

		// Replicate the vanilla top plane since we can't assume that it'll be rendered.
		// TODO: Remove vanilla top plane
		buildTopPlane(consumer, 384);

		// Always make the bottom plane have a radius of 384, to match the top plane.
		buildBottomPlane(consumer, 384);
	}

	public void renderHorizon(Matrix4fc modelView, Matrix4fc projection, Vector4f fogColor) {
		if (currentRenderDistance != Minecraft.getInstance().options.getEffectiveRenderDistance()) {
			currentRenderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
			rebuildBuffer();
		}

		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(indexCount);
		GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(modelView, fogColor, new Vector3f(), RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky", Minecraft.getInstance().getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
			Minecraft.getInstance().getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", gpuBufferSlice);

			for(int i = 0; i < 12; ++i) {
				GpuTextureView gpuTextureView3 = RenderSystem.getShaderTexture(i);
				if (gpuTextureView3 != null) {
					pass.bindSampler("Sampler" + i, gpuTextureView3);
				}
			}

			pass.setVertexBuffer(0, buffer);
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.setPipeline(RenderPipelines.SKY);
			pass.drawIndexed(0, 0, indexCount, 1);
		}
	}

	public void destroy() {
		buffer.close();
	}
}
