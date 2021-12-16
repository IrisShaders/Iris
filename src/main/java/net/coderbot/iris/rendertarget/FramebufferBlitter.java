package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

public class FramebufferBlitter {
	public static void copyFramebufferContent(GlFramebuffer from, RenderTarget to) {
		from.bindAsReadBuffer();
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, to.frameBufferId);

		int width = to.width;
		int height = to.height;

		GlStateManager._glBlitFrameBuffer(0, 0, width, height, 0, 0, width, height,
			GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, GL11C.GL_NEAREST);
	}

	public static void copyDepthBufferContent(GlFramebuffer from, RenderTarget to) {
		from.bindAsReadBuffer();
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, to.frameBufferId);

		int width = to.width;
		int height = to.height;

		// TODO: Support OpenGL 2.1?
		IrisRenderSystem.blitFramebuffer(0, 0, width, height, 0, 0, width, height,
				GL11C.GL_DEPTH_BUFFER_BIT, GL11C.GL_NEAREST);
	}
}
