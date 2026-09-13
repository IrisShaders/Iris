package net.irisshaders.iris.gl.blending;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;

public class BlendModeStorage {
	private static final boolean[] originalBlendEnable = new boolean[GlStateManagerAccessor.getBLEND_ENABLE().length];
	private static BlendMode originalBlend;
	private static boolean blendLocked;
	private static boolean blendUnknown;

	public static boolean isBlendLocked() {
		return blendLocked;
	}

	public static boolean isBlendUnknown() {
		return blendUnknown;
	}

	public static void overrideBlend(BlendMode override) {
		if (!blendLocked) {
			// Only save the previous state if the blend mode wasn't already locked
			GlStateManager.BlendState blendState = GlStateManagerAccessor.getBLEND();

			System.arraycopy(GlStateManagerAccessor.getBLEND_ENABLE(), 0, originalBlendEnable, 0, originalBlendEnable.length);
			originalBlend = new BlendMode(blendState.srcRgb, blendState.dstRgb, blendState.srcAlpha, blendState.dstAlpha);
		}

		blendLocked = false;

		if (override == null) {
			IrisRenderSystem.disableBlend();
		} else {
			IrisRenderSystem.enableBlend();
			GlStateManager._blendFuncSeparate(override.srcRgb(), override.dstRgb(), override.srcAlpha(), override.dstAlpha());
			blendUnknown = false;
		}

		blendLocked = true;
	}

	public static void overrideBufferBlend(int index, BlendMode override) {
		if (!blendLocked) {
			// Only save the previous state if the blend mode wasn't already locked
			GlStateManager.BlendState blendState = GlStateManagerAccessor.getBLEND();

			System.arraycopy(GlStateManagerAccessor.getBLEND_ENABLE(), 0, originalBlendEnable, 0, originalBlendEnable.length);
			originalBlend = new BlendMode(blendState.srcRgb, blendState.dstRgb, blendState.srcAlpha, blendState.dstAlpha);
		}

		if (override == null) {
			IrisRenderSystem.disableBufferBlend(index);
		} else {
			IrisRenderSystem.enableBufferBlend(index);
			IrisRenderSystem.blendFuncSeparatei(index, override.srcRgb(), override.dstRgb(), override.srcAlpha(), override.dstAlpha());
		}

		blendUnknown = true;
		blendLocked = true;
	}

	public static void deferBlendModeToggle(int index, boolean enabled) {
		originalBlendEnable[index] = enabled;
	}

	public static void deferBlendFunc(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
		originalBlend = new BlendMode(srcRgb, dstRgb, srcAlpha, dstAlpha);
	}

	public static void restoreBlend() {
		if (!blendLocked && !blendUnknown) {
			return;
		}

		blendLocked = false;

		for (int i = 0; i < originalBlendEnable.length; i++) {
			if (originalBlendEnable[i]) {
				GlStateManager._enableBlend(i);
			} else {
				GlStateManager._disableBlend(i);
			}
		}

		GlStateManager._blendFuncSeparate(originalBlend.srcRgb(), originalBlend.dstRgb(),
			originalBlend.srcAlpha(), originalBlend.dstAlpha());
		blendUnknown = false;
	}
}
