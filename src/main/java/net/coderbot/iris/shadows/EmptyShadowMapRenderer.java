package net.coderbot.iris.shadows;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.rendertarget.DepthTexture;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class EmptyShadowMapRenderer {
	private final DepthTexture depthTexture;

	public EmptyShadowMapRenderer(int size) {
		this.depthTexture = new DepthTexture(size, size);

		GlFramebuffer framebuffer = new GlFramebuffer();
		framebuffer.addDepthAttachment(depthTexture.getTextureId());

		framebuffer.bind();

		// I'm assuming that clearDepth is set to 1.0 here. Otherwise, we will have issues...
		GL20C.glClear(GL20C.GL_DEPTH_BUFFER_BIT);

		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);

		framebuffer.destroy();
	}

	public int getDepthTextureId() {
		return depthTexture.getTextureId();
	}

	public void destroy() {
		this.depthTexture.destroy();
	}
}
