package net.coderbot.iris.shadows;

import net.coderbot.iris.pipeline.ShadowRenderer;

public class ShadowRenderingStatus {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}
}
