package net.coderbot.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.minecraft.Util;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryUtil;

import java.util.Locale;

public interface DepthCopyStrategy {
	boolean needsMesaWorkaround = (GlUtil.getRenderer().toLowerCase(Locale.ROOT).contains("amd") || GlUtil.getRenderer().toLowerCase(Locale.ROOT).contains("radeon")) && Util.getPlatform().equals(Util.OS.LINUX);

	// FB -> T
	class Gl20CopyTexture implements DepthCopyStrategy {
		private Gl20CopyTexture() {
			// private
		}

		@Override
		public boolean needsDestFramebuffer() {
			return false;
		}

		@Override
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int internalFormat, int width, int height) {
			sourceFb.bindAsReadBuffer();

			int previousTexture = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
			RenderSystem.bindTexture(destTexture);

			GlStateManager._glCopyTexSubImage2D(
				// target
				GL20C.GL_TEXTURE_2D,
				// level
				0,
				// xoffset, yoffset
				0, 0,
				// x, y
				0, 0,
				// width
				width,
				// height
				height);

			RenderSystem.bindTexture(previousTexture);
		}
	}

	// FB -> FB
	class Gl30BlitFbCombinedDepthStencil implements DepthCopyStrategy {
		private Gl30BlitFbCombinedDepthStencil() {
			// private
		}

		@Override
		public boolean needsDestFramebuffer() {
			return true;
		}

		@Override
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int internalFormat, int width, int height) {
			sourceFb.bindAsReadBuffer();
			destFb.bindAsDrawBuffer();

			GL30C.glBlitFramebuffer(0, 0, width, height,
				0, 0, width, height,
				GL30C.GL_DEPTH_BUFFER_BIT | GL30C.GL_STENCIL_BUFFER_BIT,
				GL30C.GL_NEAREST);
		}
	}

	class Gl20FallbackCopyTexImage2D implements DepthCopyStrategy {
		private Gl20FallbackCopyTexImage2D() {
			// private
		}

		@Override
		public boolean needsDestFramebuffer() {
			return false;
		}

		@Override
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int internalFormat, int width, int height) {
			sourceFb.bindAsReadBuffer();
			RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
			RenderSystem.bindTexture(destTexture);
			IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, internalFormat, 0, 0, width, height, 0);
			RenderSystem.bindTexture(0);
		}
	}

	// T -> T
	// Fastest
	class Gl43CopyImage implements DepthCopyStrategy {
		private Gl43CopyImage() {
			// private
		}

		@Override
		public boolean needsDestFramebuffer() {
			return false;
		}

		@Override
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int internalFormat, int width, int height) {
			GL43C.glCopyImageSubData(
				sourceTexture,
				GL43C.GL_TEXTURE_2D,
				0,
				0,
				0,
				0,
				destTexture,
				GL43C.GL_TEXTURE_2D,
				0,
				0,
				0,
				0,
				width,
				height,
				1
			);
		}
	}

	static DepthCopyStrategy fastest(boolean combinedStencilRequired) {
		// TODO: Figure out the underlying issue (possible Mesa bug?) and fix
		// This is a temporary workaround for other copy methods not working on Mesa
		if (needsMesaWorkaround) {
			return new Gl20FallbackCopyTexImage2D();
		}

		// Check whether glCopyImageSubData is available by checking the function directly...
		// Gl.getCapabilities().OpenGL43 can be false even if OpenGL 4.3 functions are supported,
		// because Minecraft requests an OpenGL 3.2 forward compatible function.
		//
		// Perhaps calling GL43.isAvailable would be a different option, but we only need one
		// function, so we just check for that function.
		if (GL.getCapabilities().glCopyImageSubData != MemoryUtil.NULL) {
			return new Gl43CopyImage();
		}

		if (combinedStencilRequired) {
			return new Gl30BlitFbCombinedDepthStencil();
		} else {
			return new Gl20CopyTexture();
		}
	}

	boolean needsDestFramebuffer();

	/**
	 * Executes the copy. May or may not clobber GL_READ_FRAMEBUFFER and GL_DRAW_FRAMEBUFFER bindings - the caller is
	 * responsible for ensuring that they are restored to sensible values, or that the previous values are not relied
	 * on. The callee is responsible for ensuring that texture bindings are not modified.
	 *
	 * @param destFb The destination framebuffer. If {@link #needsDestFramebuffer()} returns false, then this param
	 *               will not be used, and it can be null.
	 */
	void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int internalFormat, int width, int height);
}
