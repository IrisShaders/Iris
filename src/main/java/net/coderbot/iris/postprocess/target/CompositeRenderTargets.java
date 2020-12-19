package net.coderbot.iris.postprocess.target;

import java.util.function.Supplier;

import net.coderbot.iris.gl.texture.InternalTextureFormat;

public class CompositeRenderTargets {
	/**
	 * The maximum number of render targets supported by Iris.
	 */
	public static int MAX_RENDER_TARGETS = 8;

	private final CompositeRenderTarget[] targets;

	public CompositeRenderTargets(int width, int height) {
		Supplier<CompositeRenderTarget> colorTarget =
				() -> CompositeRenderTarget.builder().setDimensions(width, height).build();

		// TODO: Only use RGBA32F if gdepth is explicitly specified as opposed to colortex1
		Supplier<CompositeRenderTarget> depthTarget =
				() -> CompositeRenderTarget.builder().setDimensions(width, height).setInternalFormat(InternalTextureFormat.RGBA32F).build();

		// TODO: Don't always try to create all 8 draw buffers if they aren't actually necessary
		targets = new CompositeRenderTarget[] {
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
	}

	public CompositeRenderTarget get(int index) {
		return targets[index];
	}
}
