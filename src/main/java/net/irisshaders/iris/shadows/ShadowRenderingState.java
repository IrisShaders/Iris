package net.irisshaders.iris.shadows;

import net.irisshaders.iris.pipeline.ShadowRenderer;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}
}
