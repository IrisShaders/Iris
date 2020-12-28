package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import net.minecraft.client.gl.Framebuffer;

public class FramebufferBlitter {
	public static void copyFramebufferContent(GlFramebuffer from, Framebuffer to) {
		from.bindAsReadBuffer();
		GlStateManager.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, to.fbo);

		int width = to.textureWidth;
		int height = to.textureHeight;

		// TODO: Support OpenGL 2.1?
		GL30C.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
			GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, GL11C.GL_NEAREST);
	}
}
