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
		// TODO(21w10a): Replace alpha test
		throw new IllegalStateException("Cannot setup alpha state on core profile");
		/*GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(function.getGlId(), reference);*/
	}

	public static void teardown() {
		// TODO(21w10a): Replace alpha test
		throw new IllegalStateException("Cannot teardown alpha state on core profile");
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
			// no-op, alpha test doesn't exist on core profile
			// TODO(21w10a): GlStateManager.disableAlphaTest();
		}

		@Override
		public String toString() {
			return "AlphaTestOverride { off }";
		}
	}
}
