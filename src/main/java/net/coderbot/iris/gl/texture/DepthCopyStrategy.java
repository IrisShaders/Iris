package net.coderbot.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryUtil;

public interface DepthCopyStrategy {
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
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int width, int height) {
			sourceFb.bindAsReadBuffer();

			int previousTexture = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;

			IrisRenderSystem.copyTexSubImage2D(
				destTexture,
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
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int width, int height) {
			IrisRenderSystem.blitFramebuffer(sourceFb.getId(), destFb.getId(), 0, 0, width, height,
				0, 0, width, height,
				GL30C.GL_DEPTH_BUFFER_BIT | GL30C.GL_STENCIL_BUFFER_BIT,
				GL30C.GL_NEAREST);
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
		public void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int width, int height) {
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
	void copy(GlFramebuffer sourceFb, int sourceTexture, GlFramebuffer destFb, int destTexture, int width, int height);
}
