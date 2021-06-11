package net.coderbot.iris.gl.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import org.lwjgl.opengl.GL30C;

import java.util.Arrays;

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

		if (buffers.length > 8) {
			// TODO: Adjust the limit based on the system
			throw new IllegalArgumentException("Cannot write to more than 8 draw buffers");
		}

		for (int buffer : buffers) {
			if (buffer >= 8) {
				// TODO: this shouldn't be permitted.
				Iris.logger.warn("Ignoring draw buffer " + buffer + " from draw buffers array " +
						Arrays.toString(buffers) + " since Iris doesn't support extended color buffers yet.");

				// Don't write anything here.
				glBuffers[index++] = GL30C.GL_NONE;

				continue;
			}

			glBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
		}

		GL30C.glDrawBuffers(glBuffers);
	}

	public void readBuffer(int buffer) {
		bind();

		GL30C.glReadBuffer(GL30C.GL_COLOR_ATTACHMENT0 + buffer);
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

	public void bindAsDrawBuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, getGlId());
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
