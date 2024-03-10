package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.helpers.VertexBufferHelper;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private final VertexBuffer quad;

	private FullScreenQuadRenderer() {
		// 1 quad * vertex size in bytes * 6 vertices per quad (2 triangles) = initial allocation
		// TODO: We don't do a full initial allocation?
		BufferBuilder bufferBuilder = new BufferBuilder(DefaultVertexFormat.POSITION_TEX.getVertexSize());
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(1.0F, 0.0F, 0.0F).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(1.0F, 1.0F, 0.0F).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex(0.0F, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
		BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

		quad = new VertexBuffer(VertexBuffer.Usage.STATIC);
		quad.bind();
		quad.upload(renderedBuffer);
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
		BufferUploader.reset();
		quad.bind();
	}

	public void renderQuad() {
		IrisRenderSystem.overridePolygonMode();
		quad.draw();
		IrisRenderSystem.restorePolygonMode();
	}

	public void end() {
		// NB: No need to clear the buffer state by calling glDisableVertexAttribArray - this VAO will always
		// have the same format, and buffer state is only associated with a given VAO, so we can keep it bound.
		//
		// Using quad.getFormat().clearBufferState() causes some Intel drivers to freak out:
		// https://github.com/IrisShaders/Iris/issues/1214

		RenderSystem.enableDepthTest();
		((VertexBufferHelper) quad).restoreBinding();
	}
}
