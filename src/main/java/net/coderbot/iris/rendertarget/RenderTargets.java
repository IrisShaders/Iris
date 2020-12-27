package net.coderbot.iris.rendertarget;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;

public class RenderTargets {
	/**
	 * The maximum number of render targets supported by Iris.
	 */
	public static int MAX_RENDER_TARGETS = 8;

	private final RenderTarget[] targets;
	private final DepthTexture depthTexture;

	private int cachedWidth;
	private int cachedHeight;

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

		this.cachedWidth = width;
		this.cachedHeight = height;
	}

	public RenderTarget get(int index) {
		return targets[index];
	}

	public DepthTexture getDepthTexture() {
		return depthTexture;
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
	}
}
