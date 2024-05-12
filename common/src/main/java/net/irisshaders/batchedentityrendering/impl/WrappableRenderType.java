package net.irisshaders.batchedentityrendering.impl;

import net.minecraft.client.renderer.RenderType;

public interface WrappableRenderType {
	/**
	 * Returns the underlying wrapped RenderType. Might return itself if this RenderType doesn't wrap anything.
	 */
	RenderType unwrap();
}
