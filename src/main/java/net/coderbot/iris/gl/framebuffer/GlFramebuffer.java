package net.coderbot.iris.gl.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.coderbot.iris.gl.GlResource;
import org.lwjgl.opengl.GL30C;

public class GlFramebuffer extends GlResource {
	private Int2IntMap attachments;

	public GlFramebuffer() {
		super(GlStateManager.genFramebuffers());

		this.attachments = new Int2IntArrayMap();

		bind();
	}

	public void addDepthAttachment(int texture) {
		bind();
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);
	}

	public void addColorAttachment(int index, int texture) {
		bind();
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture, 0);
		attachments.put(index, texture);
	}

	public void drawBuffers(int[] buffers) {
		bind();

		int[] glBuffers = new int[buffers.length];
		int index = 0;

		for (int buffer : buffers) {
			glBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
		}

		GL30C.glDrawBuffers(glBuffers);
	}

	public int getColorAttachment(int index) {
		return attachments.get(index);
	}

	public void bind() {
		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, getGlId());
	}

	public void bindAsReadBuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, getGlId());
	}

	protected void destroyInternal() {
		GlStateManager.deleteFramebuffers(getGlId());
	}

	public boolean isComplete() {
		bind();
		int status = GlStateManager.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);

		return status == GL30C.GL_FRAMEBUFFER_COMPLETE;
	}
}
