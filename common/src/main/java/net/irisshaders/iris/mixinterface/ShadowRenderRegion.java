package net.irisshaders.iris.mixinterface;

public interface ShadowRenderRegion {
	void swapToRegularRenderList();

	void swapToShadowRenderList();

	void iris$forceClearAllBatches();
}
