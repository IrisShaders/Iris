package net.coderbot.iris.shadows;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.rendertarget.DepthTexture;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class EmptyShadowMapRenderer {
	private final DepthTexture depthTexture;

	public EmptyShadowMapRenderer(int size) {
		this.depthTexture = new DepthTexture(size, size);

		GlStateManager.bindTexture(depthTexture.getTextureId());

		// We have to do this or else sampling a sampler2DShadow produces "undefined" results.
		//
		// For example, if this call is omitted under Mesa then it will appear as if the whole world is in shadow at all
		// times.
		//
		// TODO: Do not require OpenGL 3.0 and only enable this if shadowHardwareFiltering is enabled
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);

		// The shadow texture should be smoothed.
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		GlStateManager.bindTexture(0);

		GlFramebuffer framebuffer = new GlFramebuffer();
		framebuffer.addDepthAttachment(depthTexture.getTextureId());

		framebuffer.bind();

		// Hopefully I'm not clobbering any other OpenGL state here...
		GL20C.glClearDepth(1.0);
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
