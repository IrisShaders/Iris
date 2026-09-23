package net.irisshaders.iris.shadows;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	public static int getRenderDistance() {
		return ShadowRenderer.renderDistance;
	}
}
