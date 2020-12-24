package net.coderbot.iris;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

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
		consumer.next();
		consumer.vertex(x1, TOP, z1);
		consumer.next();
		consumer.vertex(x2, TOP, z2);
		consumer.next();
		consumer.vertex(x2, BOTTOM, z2);
		consumer.next();
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

	private void buildBottomPlane(VertexConsumer consumer, float radius) {
		consumer.vertex(radius, BOTTOM, -radius);
		consumer.next();
		consumer.vertex(-radius, BOTTOM, -radius);
		consumer.next();
		consumer.vertex(-radius, BOTTOM, radius);
		consumer.next();
		consumer.vertex(radius, BOTTOM, radius);
		consumer.next();
	}

	private void buildHorizon(VertexConsumer consumer) {
		float radius = getRenderDistanceInBlocks();

		buildRegularOctagonalPrism(consumer, radius);
		buildBottomPlane(consumer, radius);
	}

	private int getRenderDistanceInBlocks() {
		return MinecraftClient.getInstance().options.viewDistance * 16;
	}

	public void renderHorizon(MatrixStack matrices) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		// Build the horizon quads into a buffer
		buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);
		buildHorizon(buffer);
		buffer.end();

		// Render the horizon buffer
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrices.peek().getModel());
		BufferRenderer.draw(buffer);
		RenderSystem.popMatrix();
	}
}
