package net.coderbot.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.mixin.GlStateManagerAccessor;

public class BlendModeStorage {
	private static BlendMode originalBlend;
	private static boolean blendLocked;

	public static boolean isBlendLocked() {
		return blendLocked;
	}

	public static void overrideBlend(BlendMode override) {
		blendLocked = false;
		GlStateManager.BlendState blendState = GlStateManagerAccessor.getBLEND();
		originalBlend = new BlendMode(blendState.srcRgb, blendState.dstRgb, blendState.srcAlpha, blendState.dstAlpha);
		GlStateManager._blendFuncSeparate(override.getSrcRgb(), override.getDstRgb(), override.getSrcAlpha(), override.getDstAlpha());
		blendLocked = true;
	}

	public static void restoreBlend() {
		blendLocked = false;
		GlStateManager._blendFuncSeparate(originalBlend.getSrcRgb(), originalBlend.getDstRgb(), originalBlend.getSrcAlpha(), originalBlend.getDstAlpha());
	}
}
