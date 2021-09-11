package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.renderer.RenderType;

public interface WrappableRenderLayer {
	/**
	 * Returns the underlying wrapped RenderLayer. Might return itself if this RenderLayer doesn't wrap anything.
	 */
	RenderType unwrap();
}
