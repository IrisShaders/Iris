package net.coderbot.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.mixin.GlStateManagerAccessor;

public class BlendModeStorage {
	private static int[] originalBlend;
	private static boolean blendLocked;

	public static boolean isBlendLocked() {
		return blendLocked;
	}

	public static void overrideBlend(int[] override) {
		blendLocked = false;
		GlStateManager.BlendState blendState = GlStateManagerAccessor.getBLEND();
		originalBlend = new int[] { blendState.srcRgb, blendState.dstRgb, blendState.srcAlpha, blendState.dstAlpha };
		GlStateManager._blendFuncSeparate(override[0], override[1], override[2], override[3]);
		blendLocked = true;
	}

	public static void restoreBlend() {
		blendLocked = false;
		GlStateManager._blendFuncSeparate(originalBlend[0], originalBlend[1], originalBlend[2], originalBlend[3]);
	}
}
