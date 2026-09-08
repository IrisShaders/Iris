package net.irisshaders.iris.mixinterface;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

public interface ShadowRenderRegion {
	void swapToRegularRenderList();

	void swapToShadowRenderList();

	void iris$forceClearAllBatches();

	void iris$forceClearBatchFor(TerrainRenderPass pass);
}
