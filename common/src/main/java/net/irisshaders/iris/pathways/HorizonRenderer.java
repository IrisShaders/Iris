package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Renders the sky horizon. Vanilla Minecraft simply uses the "clear color" for its horizon, and then draws a plane
 * above the player. This class extends the sky rendering so that an inverted octagonal cone is drawn around the player instead,
 * allowing shaders to perform more advanced sky rendering.
 * <p>
 * However, the horizon rendering is designed so that when sky shaders are not being used, it looks almost exactly the
 * same as vanilla sky rendering, except a few almost entirely imperceptible differences where the walls
 * of the inverted octagonal cone intersect the top plane.
 */
public class HorizonRenderer {
	/**
	 * The Y coordinate of the top skybox plane. Acts as the upper bound for the horizon cone, since the cone lies
	 * between the bottom and top skybox planes.
	 */
	private static final float TOP = 16.0F;

	/**
	 * The Y coordinate of the bottom skybox plane. Acts as the lower bound for the horizon cone, since the cone lies
	 * between the bottom and top skybox planes.
	 */
	private static final float BOTTOM = -16.0F;

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

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);

		buildHorizon(currentRenderDistance * 16, buffer);
		MeshData meshData = buffer.buildOrThrow();

		this.buffer = RenderSystem.getDevice().createBuffer(() -> "Horizon", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, meshData.vertexBuffer());
		this.indexCount = meshData.drawState().indexCount();
		meshData.close();
		Tesselator.getInstance().clear();
	}

	private void buildHorizon(int radius, VertexConsumer consumer) {
		if (radius > 256) {
			// Prevent the cone from getting too large, this causes issues on some shader packs that modify the vanilla
			// sky if we don't do this.
			radius = 256;
		}

		consumer.addVertex(0.0F, BOTTOM, 0.0F);

		for (int i = 0; i <= 8; i++) {
			float angle = (float) (-i * Math.PI / 4.0);
			float x = (float) (radius * Math.cos(angle));
			float z = (float) (radius * Math.sin(angle));
			consumer.addVertex(x, TOP, z);
		}
	}

	public void renderHorizon(Matrix4fc modelView, Matrix4fc projection, Vector4f fogColor) {
		if (currentRenderDistance != Minecraft.getInstance().options.getEffectiveRenderDistance()) {
			currentRenderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
			rebuildBuffer();
		}

		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLE_FAN);
		GpuBuffer indexBuffer = indices.getBuffer(indexCount);
		GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(modelView, fogColor, new Vector3f(), new Matrix4f());
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky", Minecraft.getInstance().getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
			Minecraft.getInstance().getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", gpuBufferSlice);

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
