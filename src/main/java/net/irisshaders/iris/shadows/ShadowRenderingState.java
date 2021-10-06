package net.irisshaders.iris.shadows;

import com.mojang.math.Matrix4f;
import net.irisshaders.iris.pipeline.ShadowRenderer;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

	public static Matrix4f getShadowOrthoMatrix() {
		return ShadowRenderer.ACTIVE ? ShadowRenderer.ORTHO.copy() : null;
	}
}
