package net.coderbot.iris.shadows;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.pipeline.ShadowRenderer;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	public static Matrix4f getShadowOrthoMatrix() {
		return ShadowRenderer.ACTIVE ? ShadowRenderer.ORTHO.copy() : null;
	}
}
