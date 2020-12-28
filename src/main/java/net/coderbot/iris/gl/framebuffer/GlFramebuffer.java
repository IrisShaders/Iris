package net.coderbot.iris.gl.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.lwjgl.opengl.GL30C;

public class GlFramebuffer {
	private final int id;
	private boolean valid;
	private Int2IntMap attachments;

	public GlFramebuffer() {
		this.id = GlStateManager.genFramebuffers();
		this.valid = true;
		this.attachments = new Int2IntArrayMap();

		bind();
	}

	public void addDepthAttachment(int texture) {
		requireValid();

		bind();
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);
	}

	public void addColorAttachment(int index, int texture) {
		requireValid();

		bind();
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture, 0);
		attachments.put(index, texture);
	}

	public void drawBuffers(int[] buffers) {
		requireValid();

		int[] glBuffers = new int[buffers.length];
		int index = 0;

		for (int buffer : buffers) {
			glBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
		}

		bind();
		GL30C.glDrawBuffers(glBuffers);
	}

	public int getColorAttachment(int index) {
		return attachments.get(index);
	}

	public void bind() {
		requireValid();

		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, this.id);
	}

	public void bindAsReadBuffer() {
		requireValid();

		GlStateManager.bindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, this.id);
	}

	public void delete() {
		requireValid();

		GlStateManager.deleteFramebuffers(this.id);
		valid = false;
	}

	public boolean isComplete() {
		int status = GlStateManager.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);

		return status == GL30C.GL_FRAMEBUFFER_COMPLETE;
	}

	private void requireValid() {
		if (!valid) {
			throw new IllegalStateException("Attempted to use a deleted framebuffer!");
		}
	}
}
