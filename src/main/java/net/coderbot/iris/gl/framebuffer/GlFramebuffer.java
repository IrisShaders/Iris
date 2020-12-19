package net.coderbot.iris.gl.framebuffer;

import java.nio.ByteBuffer;

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

		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);
	}

	public void addDepthAttachment(int width, int height) {
		int depthTexture = GL30C.glGenTextures();
		GlStateManager.bindTexture(depthTexture);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE);
		GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE);
		GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, GL30C.GL_DEPTH_COMPONENT, width, height, 0, GL30C.GL_DEPTH_COMPONENT, GL30C.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GlStateManager.bindTexture(0);
	}

	public void addColorAttachment(int index, int texture) {
		requireValid();

		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture, 0);
		attachments.put(index, texture);
	}

	public int getColorAttachment(int index) {
		return attachments.get(index);
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
