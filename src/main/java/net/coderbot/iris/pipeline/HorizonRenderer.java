package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/**
 * Renders the sky horizon. Vanilla Minecraft simply uses the "clear color" for its horizon, and then draws a plane
 * above the player. This class extends the sky rendering so that an octagonal prism is drawn around the player instead,
 * allowing shaders to perform more advanced sky rendering.
 * <p>
 * However, the horizon rendering is designed so that when sky shaders are not being used, it looks almost exactly the
 * same as vanilla sky rendering, with the exception of a few almost entirely imperceptible differences where the walls
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
	private static final double COS_22_5 = Math.cos(Math.toRadians(22.5));

	/**
	 * Sine of 22.5 degrees.
	 */
	private static final double SIN_22_5 = Math.sin(Math.toRadians(22.5));

	public HorizonRenderer() {
	}

	private void buildQuad(VertexConsumer consumer, double x1, double z1, double x2, double z2) {
		consumer.vertex(x1, BOTTOM, z1);
		consumer.endVertex();
		consumer.vertex(x1, TOP, z1);
		consumer.endVertex();
		consumer.vertex(x2, TOP, z2);
		consumer.endVertex();
		consumer.vertex(x2, BOTTOM, z2);
		consumer.endVertex();
	}

	private void buildHalf(VertexConsumer consumer, double adjacent, double opposite, boolean invert) {
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
	private void buildOctagonalPrism(VertexConsumer consumer, double adjacent, double opposite) {
		buildHalf(consumer, adjacent, opposite, false);
		buildHalf(consumer, adjacent, opposite, true);
	}

	private void buildRegularOctagonalPrism(VertexConsumer consumer, double radius) {
		buildOctagonalPrism(consumer, radius * COS_22_5, radius * SIN_22_5);
	}

	private void buildBottomPlane(VertexConsumer consumer, int radius) {
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.vertex(x + 64, BOTTOM, z);
				consumer.endVertex();
				consumer.vertex(x, BOTTOM, z);
				consumer.endVertex();
				consumer.vertex(x, BOTTOM, z + 64);
				consumer.endVertex();
				consumer.vertex(x + 64, BOTTOM, z + 64);
				consumer.endVertex();
			}
		}
	}

	private void buildTopPlane(VertexConsumer consumer, int radius) {
		// You might be tempted to try to combine this with buildBottomPlane to avoid code duplication,
		// but that won't work since the winding order has to be reversed or else one of the planes will be
		// discarded by back face culling.
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.vertex(x + 64, TOP, z);
				consumer.endVertex();
				consumer.vertex(x + 64, TOP, z + 64);
				consumer.endVertex();
				consumer.vertex(x, TOP, z + 64);
				consumer.endVertex();
				consumer.vertex(x, TOP, z);
				consumer.endVertex();
			}
		}
	}

	private void buildHorizon(VertexConsumer consumer) {
		int radius = getRenderDistanceInBlocks();

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

	private int getRenderDistanceInBlocks() {
		return Minecraft.getInstance().options.renderDistance * 16;
	}

	public void renderHorizon(Matrix4f matrix) {
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();

		// Build the horizon quads into a buffer
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
		buildHorizon(buffer);
		buffer.end();

		// Render the horizon buffer
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrix);
		BufferUploader.end(buffer);
		RenderSystem.popMatrix();
	}
}
