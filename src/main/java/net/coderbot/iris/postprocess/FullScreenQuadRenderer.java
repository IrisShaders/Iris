package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	private final VertexBuffer quad;

	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {
		this.quad = createQuad();
	}

	/**
	 * Renders a quad to the screen. Textures are enabled if they aren't already enabled, and identity modelview /
	 * projection matrices will be used.
	 */
	public void render() {
		begin();

		RenderSystem.enableTexture();
		renderQuad();

		end();
	}

	private void begin() {
		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}

	private void renderQuad() {
		quad.bind();
		VertexFormats.POSITION_COLOR_TEXTURE.startDrawing(0L);
		quad.draw(IDENTITY, GL11C.GL_TRIANGLES);
		VertexFormats.POSITION_COLOR_TEXTURE.endDrawing();
		VertexBuffer.unbind();
	}

	private void end() {
		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}

	/**
	 * Creates and uploads a vertex buffer containing a single full-screen quad
	 */
	private static VertexBuffer createQuad() {
		VertexBuffer quad = new VertexBuffer(VertexFormats.POSITION_COLOR_TEXTURE);

		BufferBuilder buffer = new BufferBuilder(6 * VertexFormats.POSITION_COLOR_TEXTURE.getVertexSizeInteger());
		buffer.begin(GL11C.GL_TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE);

		// NB: Use counterclockwise order here! Otherwise these triangles will be invisible.

		// The first triangle
		vertex(buffer, true, true);
		vertex(buffer, false, true);
		vertex(buffer, true, false);

		// The second triangle
		vertex(buffer, true, false);
		vertex(buffer, false, true);
		vertex(buffer, false, false);

		buffer.end();
		quad.upload(buffer);

		return quad;
	}

	private static void vertex(BufferBuilder buffer, boolean plusX, boolean up) {
		// These coordinates are provided in clip space, and therefore range from -1.0 to 1.0 to fill the entire screen.
		buffer.vertex(plusX ? 1.0F : -1.0F, up ? 1.0F : -1.0F, 0.0);

		// Each vertex will always be white.
		buffer.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Texture coordinates are in the range of 0.0 to 1.0
		buffer.texture(plusX ? 1.0F : 0.0F, up ? 1.0F : 0.0F);

		// Move to the next vertex.
		buffer.next();
	}

	/**
	 * An identity matrix.
	 */
	private static final Matrix4f IDENTITY;

	static {
		Matrix4f identity = new Matrix4f();
		identity.loadIdentity();
		IDENTITY = identity;
	}
}
