package net.irisshaders.iris.gl.blending;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;

public class DepthColorStorage {
	private static boolean originalDepthEnable;
	private static int originalColor;
	private static boolean depthColorLocked;

	public static boolean isDepthColorLocked() {
		return depthColorLocked;
	}

	public static void disableDepthColor() {
		if (!depthColorLocked) {
			// Only save the previous state if the depth and color mask wasn't already locked
			int colorMask = GlStateManagerAccessor.getCOLOR_MASK();
			GlStateManager.DepthState depthState = GlStateManagerAccessor.getDEPTH();

			originalDepthEnable = depthState.mask;
			originalColor = colorMask;
		}

		depthColorLocked = false;

		GlStateManager._depthMask(false);
		GlStateManager._colorMask(0);

		depthColorLocked = true;
	}

	public static void deferDepthEnable(boolean enabled) {
		originalDepthEnable = enabled;
	}

	public static void deferColorMask(int writeMask) {
		originalColor = writeMask;
	}

	public static void unlockDepthColor() {
		if (!depthColorLocked) {
			return;
		}

		depthColorLocked = false;

		GlStateManager._depthMask(originalDepthEnable);

		GlStateManager._colorMask(originalColor);
	}
}
