package net.coderbot.iris.fantastic;

import net.minecraft.client.render.RenderLayer;

public interface WrappableRenderLayer {
	/**
	 * Returns the underlying wrapped RenderLayer. Might return itself if this RenderLayer doesn't wrap anything.
	 */
	RenderLayer unwrap();
}
