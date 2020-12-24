package net.coderbot.iris.postprocess.target;

import java.util.function.Supplier;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;

public class CompositeRenderTargets {
	/**
	 * The maximum number of render targets supported by Iris.
	 */
	public static int MAX_RENDER_TARGETS = 8;

	private final CompositeRenderTarget[] targets;
	private final DepthTexture depthTexture;

	private int cachedWidth;
	private int cachedHeight;

	public CompositeRenderTargets(int width, int height) {
		Supplier<CompositeRenderTarget> colorTarget =
			() -> CompositeRenderTarget.builder().setDimensions(width, height).build();

		// TODO: Only use RGBA32F if gdepth is explicitly specified as opposed to colortex1
		Supplier<CompositeRenderTarget> depthTarget =
			() -> CompositeRenderTarget.builder().setDimensions(width, height).setInternalFormat(InternalTextureFormat.RGBA32F).build();

		// TODO: Don't always try to create all 8 draw buffers if they aren't actually necessary
		targets = new CompositeRenderTarget[]{
			colorTarget.get(),
			depthTarget.get(),
			colorTarget.get(),
			colorTarget.get(),
			colorTarget.get(),
			colorTarget.get(),
			colorTarget.get(),
			colorTarget.get()
		};

		if (targets.length != MAX_RENDER_TARGETS) {
			// TODO: Just temporary
			throw new AssertionError();
		}

		this.depthTexture = new DepthTexture(width, height);

		this.cachedWidth = width;
		this.cachedHeight = height;
	}

	public CompositeRenderTarget get(int index) {
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

		for (CompositeRenderTarget target : targets) {
			target.resize(newWidth, newHeight);
		}

		depthTexture.resize(newWidth, newHeight);
	}
}
