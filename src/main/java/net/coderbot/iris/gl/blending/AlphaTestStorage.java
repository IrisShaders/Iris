package net.coderbot.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;

public class AlphaTestStorage {
	private static boolean originalAlphaTestEnable;
	private static AlphaTest originalAlphaTest;
	private static boolean alphaTestLocked;

	public static boolean isAlphaTestLocked() {
		return alphaTestLocked;
	}

	public static void overrideAlphaTest(AlphaTest override) {
		/*if (!alphaTestLocked) {
			// Only save the previous state if the alpha test wasn't already locked
			GlStateManager.AlphaState alphaState = GlStateManagerAccessor.getALPHA_TEST();

			originalAlphaTestEnable = ((BooleanStateAccessor) alphaState.mode).isEnabled();
			originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(alphaState.func).get(), alphaState.reference);
		}

		alphaTestLocked = false;

		if (override == null) {
			GlStateManager._disableAlphaTest();
		} else {
			GlStateManager._enableAlphaTest();
			GlStateManager._alphaFunc(override.getFunction().getGlId(), override.getReference());
		}

		alphaTestLocked = true;*/
	}

	public static void deferAlphaTestToggle(boolean enabled) {
		originalAlphaTestEnable = enabled;
	}

	public static void deferAlphaFunc(int function, float reference) {
		originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(function).get(), reference);
	}

	public static void restoreAlphaTest() {
		/*if (!alphaTestLocked) {
			return;
		}

		alphaTestLocked = false;

		if (originalAlphaTestEnable) {
			GlStateManager._enableAlphaTest();
		} else {
			GlStateManager._disableAlphaTest();
		}

		GlStateManager._alphaFunc(originalAlphaTest.getFunction().getGlId(), originalAlphaTest.getReference());*/
	}
}
