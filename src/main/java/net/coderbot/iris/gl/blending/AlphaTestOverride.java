package net.coderbot.iris.gl.blending;

import com.mojang.blaze3d.platform.GlStateManager;

public class AlphaTestOverride {
	private final AlphaTestFunction function;
	private final float reference;

	public AlphaTestOverride(AlphaTestFunction function, float reference) {
		this.function = function;
		this.reference = reference;
	}

	public void setup() {
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(function.getGlId(), reference);
	}

	public static void teardown() {
		GlStateManager.disableAlphaTest();
		GlStateManager.alphaFunc(AlphaTestFunction.GREATER.getGlId(), 0.1F);
	}

	@Override
	public String toString() {
		return "AlphaTestOverride { " + function + " " + reference + " }";
	}

	public static class Off extends AlphaTestOverride {
		public Off() {
			super(null, 0.0f);
		}

		@Override
		public void setup() {
			GlStateManager.disableAlphaTest();
		}

		@Override
		public String toString() {
			return "AlphaTestOverride { off }";
		}
	}
}
