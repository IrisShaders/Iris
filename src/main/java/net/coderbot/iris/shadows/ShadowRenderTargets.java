package net.coderbot.iris.shadows;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.rendertarget.DepthTexture;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.nio.IntBuffer;
import java.util.Arrays;

public class ShadowRenderTargets {
	// TODO: Make this match the value of GL_MAX_DRAW_BUFFERS (or whatever property name it is)
	public static int MAX_SHADOW_RENDER_TARGETS = 8;

	private final int[] targets;
	private final InternalTextureFormat[] formats;

	private final DepthTexture depthTexture;
	private final DepthTexture noTranslucents;

	private final GlFramebuffer framebuffer;
	private final GlFramebuffer noTranslucentFB;
	private final int resolution;
	private static final IntBuffer NULL_BUFFER = null;
	private boolean firstTranslucentCopy;
	private static final boolean supportsFramebufferBlitting = GL.getCapabilities().OpenGL30 || GL.getCapabilities().GL_EXT_framebuffer_blit;

	public ShadowRenderTargets(int resolution, InternalTextureFormat[] formats) {
		if (formats.length > MAX_SHADOW_RENDER_TARGETS) {
			throw new IllegalStateException("Too many shadow render targets, requested " + formats.length +
					" but only " + MAX_SHADOW_RENDER_TARGETS + " are allowed.");
		}

		this.formats = Arrays.copyOf(formats, formats.length);
		this.resolution = resolution;

		int[] drawBuffers = new int[formats.length];

		targets = new int[formats.length];
		GlStateManager._genTextures(targets);

		depthTexture = new DepthTexture(resolution, resolution, DepthBufferFormat.DEPTH);
		noTranslucents = new DepthTexture(resolution, resolution, DepthBufferFormat.DEPTH);

		this.framebuffer = new GlFramebuffer();
		this.noTranslucentFB = new GlFramebuffer();

		framebuffer.addDepthAttachment(depthTexture.getTextureId());
		noTranslucentFB.addDepthAttachment(noTranslucents.getTextureId());

		for (int i = 0; i < formats.length; i++) {
			InternalTextureFormat format = formats[i];

			RenderSystem.bindTexture(targets[i]);

			GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, format.getGlFormat(), resolution, resolution, 0,
				PixelFormat.RGBA.getGlFormat(), PixelType.UNSIGNED_BYTE.getGlFormat(), NULL_BUFFER);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);

			framebuffer.addColorAttachment(i, targets[i]);
			noTranslucentFB.addColorAttachment(i, targets[i]);
			drawBuffers[i] = i;
		}

		framebuffer.drawBuffers(drawBuffers);
		noTranslucentFB.drawBuffers(drawBuffers);

		this.firstTranslucentCopy = true;

		RenderSystem.bindTexture(0);
	}

	public void copyPreTranslucentDepth() {
		// Copy the content of the depth texture before rendering translucent content.
		// This is needed for the shadowtex0 / shadowtex1 split.

		// note: destFb is null since we never end up getting a strategy that requires the target framebuffer
		// this is a bit of an assumption but it works for now
		if (firstTranslucentCopy && supportsFramebufferBlitting) {
			firstTranslucentCopy = false;
			framebuffer.bindAsReadBuffer();
			noTranslucentFB.bindAsDrawBuffer();
			GlStateManager._glBlitFrameBuffer(0, 0, resolution, resolution,
				0, 0, resolution, resolution,
				GL30C.GL_DEPTH_BUFFER_BIT,
				GL30C.GL_NEAREST);
		} else {
			DepthCopyStrategy.fastest(false).copy(framebuffer, depthTexture.getTextureId(), null,
				noTranslucents.getTextureId(), resolution, resolution);
		}
	}

	public GlFramebuffer getFramebuffer() {
		return framebuffer;
	}

	public DepthTexture getDepthTexture() {
		return depthTexture;
	}

	public DepthTexture getDepthTextureNoTranslucents() {
		return noTranslucents;
	}

	public int getNumColorTextures() {
		return targets.length;
	}

	public int getColorTextureId(int index) {
		return targets[index];
	}

	public InternalTextureFormat getColorTextureFormat(int index) {
		return formats[index];
	}

	public void destroy() {
		framebuffer.destroy();

		GlStateManager._deleteTextures(targets);
		depthTexture.destroy();
		noTranslucents.destroy();
	}
}
