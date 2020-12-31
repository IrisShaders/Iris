package net.coderbot.iris.rendertarget;

import java.util.Arrays;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.shaderpack.ShaderPack;

import net.minecraft.client.gl.Framebuffer;

public class RenderTargets {
	/**
	 * The maximum number of render targets supported by Iris.
	 */
	public static int MAX_RENDER_TARGETS = 8;

	private final RenderTarget[] targets;
	private final DepthTexture depthTexture;
	private final DepthTexture noTranslucents;

	private int cachedWidth;
	private int cachedHeight;

	public RenderTargets(Framebuffer reference, ShaderPack pack) {
		this(reference.textureWidth, reference.textureHeight, pack.getPackDirectives().getRequestedBufferFormats());
	}

	public RenderTargets(int width, int height, InternalTextureFormat[] formats) {
		if (formats.length > MAX_RENDER_TARGETS) {
			throw new IllegalArgumentException("Too many render targets: " + formats.length + " targets requested, but the maximum number of render targets is " + MAX_RENDER_TARGETS);
		}

		targets = new RenderTarget[formats.length];
		int targetIndex = 0;

		for (InternalTextureFormat format : formats) {
			targets[targetIndex++] = RenderTarget.builder().setDimensions(width, height).setInternalFormat(format).build();
		}

		this.depthTexture = new DepthTexture(width, height);
		this.noTranslucents = new DepthTexture(width, height);

		this.cachedWidth = width;
		this.cachedHeight = height;
	}

	public void destroy() {
		// TODO: This is a hack to make things not break on reload
		// It seems like something is holding on to colortex0/depthtex0 somewhere
		boolean first = true;

		for (RenderTarget target : targets) {
			if (first) {
				first = false;
				continue;
			}

			target.destroy();
		}

		// depthTexture.destroy();
		noTranslucents.destroy();
	}

	public RenderTarget get(int index) {
		return targets[index];
	}

	public DepthTexture getDepthTexture() {
		return depthTexture;
	}

	public DepthTexture getDepthTextureNoTranslucents() {
		return noTranslucents;
	}

	public void resizeIfNeeded(int newWidth, int newHeight) {
		if (newWidth == cachedWidth && newHeight == cachedHeight) {
			// No resize needed
			return;
		}

		Iris.logger.info("Resizing render targets to " + newWidth + "x" + newHeight);
		cachedWidth = newWidth;
		cachedHeight = newHeight;

		for (RenderTarget target : targets) {
			target.resize(newWidth, newHeight);
		}

		depthTexture.resize(newWidth, newHeight);
		noTranslucents.resize(newWidth, newHeight);
	}

	public GlFramebuffer createFramebufferWritingToMain(int[] drawBuffers) {
		return createFullFramebuffer(false, drawBuffers);
	}

	public GlFramebuffer createFramebufferWritingToAlt(int[] drawBuffers) {
		return createFullFramebuffer(true, drawBuffers);
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		boolean[] stageWritesToAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		Arrays.fill(stageWritesToAlt, clearsAlt);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToAlt, drawBuffers);

		framebuffer.addDepthAttachment(this.getDepthTexture().getTextureId());

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(boolean[] stageWritesToAlt, int[] drawBuffers) {
		GlFramebuffer framebuffer = new GlFramebuffer();

		for (int i = 0; i < RenderTargets.MAX_RENDER_TARGETS; i++) {
			RenderTarget target = this.get(i);

			int textureId = stageWritesToAlt[i] ? target.getAltTexture() : target.getMainTexture();

			framebuffer.addColorAttachment(i, textureId);
		}

		if (!framebuffer.isComplete()) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		framebuffer.drawBuffers(drawBuffers);

		return framebuffer;
	}

	public int getCurrentWidth() {
		return cachedWidth;
	}

	public int getCurrentHeight() {
		return cachedHeight;
	}
}
