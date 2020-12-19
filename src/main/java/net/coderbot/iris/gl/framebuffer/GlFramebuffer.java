package net.coderbot.iris.gl.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL30C;

public class GlFramebuffer {
	private final int id;
	private boolean valid;

	public GlFramebuffer() {
		this.id = GlStateManager.genFramebuffers();
		this.valid = true;

		bind();
	}

	public void addDepthAttachment(int texture) {
		requireValid();

		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);
	}

	public void addColorAttachment(int index, int texture) {
		requireValid();

		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture, 0);
	}

	public void bind() {
		requireValid();

		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, this.id);
	}

	public void delete() {
		requireValid();

		GlStateManager.deleteFramebuffers(this.id);
		valid = false;
	}

	private void requireValid() {
		if (!valid) {
			throw new IllegalStateException("Attempted to use a deleted framebuffer!");
		}
	}
}
