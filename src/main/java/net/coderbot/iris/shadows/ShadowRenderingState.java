package net.coderbot.iris.shadows;

import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.util.math.Matrix4f;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	public static Matrix4f getShadowOrthoMatrix() {
		return ShadowRenderer.ACTIVE ? ShadowRenderer.ORTHO.copy() : null;
	}
}
