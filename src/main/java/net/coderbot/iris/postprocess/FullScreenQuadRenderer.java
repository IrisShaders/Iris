package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.fantastic.VertexBufferHelper;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import org.lwjgl.opengl.GL11;

import org.lwjgl.opengl.GL20C;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private VertexBuffer quad;

	private FullScreenQuadRenderer() {
		// 1 quad * vertex size in bytes * 6 vertices per quad (2 triangles) = initial allocation
		// TODO: We don't do a full initial allocation?
		BufferBuilder bufferBuilder = new BufferBuilder(VertexFormats.POSITION_TEXTURE.getVertexSize());
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(-1.0F, -1.0F, 0.0F).texture(0.0F, 0.0F).next();
		bufferBuilder.vertex(1.0F, -1.0F, 0.0F).texture(1.0F, 0.0F).next();
		bufferBuilder.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F).next();
		bufferBuilder.vertex(-1.0F, 1.0F, 0.0F).texture(0.0F, 1.0F).next();
		bufferBuilder.end();

		quad = new VertexBuffer();
		quad.bind();
		quad.upload(bufferBuilder);
		VertexBuffer.unbind();
	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	public void begin() {
		((VertexBufferHelper) quad).saveBinding();
		RenderSystem.disableDepthTest();
		BufferRenderer.unbindAll();
	}

	public void renderQuad() {
		quad.drawVertices();
	}

	public void end() {
		RenderSystem.enableDepthTest();
		quad.getElementFormat().endDrawing();
		VertexBuffer.unbind();
		VertexBuffer.unbindVertexArray();
		((VertexBufferHelper) quad).restoreBinding();
	}
}
