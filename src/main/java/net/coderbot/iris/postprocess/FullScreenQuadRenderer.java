package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL20C;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	private final int quadBuffer;

	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {
		this.quadBuffer = createQuad();
	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	public void begin() {
		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL20C.glBindBuffer(GL20C.GL_ARRAY_BUFFER, quadBuffer);
		VertexFormats.POSITION_TEXTURE.startDrawing(0L);
	}

	public void renderQuad() {
		GL20C.glDrawArrays(GL20C.GL_TRIANGLE_STRIP, 0, 4);
	}

	public static void end() {
		VertexFormats.POSITION_TEXTURE.endDrawing();
		GL20C.glBindBuffer(GL20C.GL_ARRAY_BUFFER, 0);

		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.popMatrix();
	}

	/**
	 * Creates and uploads a vertex buffer containing a single full-screen quad
	 */
	private static int createQuad() {
		float[] vertices = new float[] {
			// Vertex 0: Top right corner
			1.0F, 1.0F, 0.0F,
			1.0F, 1.0F,
			// Vertex 1: Top left corner
			-1.0F, 1.0F, 0.0F,
			0.0F, 1.0F,
			// Vertex 2: Bottom right corner
			1.0F, -1.0F, 0.0F,
			1.0F, 0.0F,
			// Vertex 3: Bottom left corner
			-1.0F, -1.0F, 0.0F,
			0.0F, 0.0F
		};

		int buffer = GL20C.glGenBuffers();

		GL20C.glBindBuffer(GL20C.GL_ARRAY_BUFFER, buffer);
		GL20C.glBufferData(GL20C.GL_ARRAY_BUFFER, vertices, GL20C.GL_STATIC_DRAW);
		GL20C.glBindBuffer(GL20C.GL_ARRAY_BUFFER, 0);

		return buffer;
	}
}
