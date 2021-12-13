package net.coderbot.iris.gl.blending;

public class AlphaTestOverride {
	public static final AlphaTestOverride OFF = new AlphaTestOverride(null);

	private final AlphaTest alphaTest;

	public AlphaTestOverride(AlphaTest alphaTest) {
		this.alphaTest = alphaTest;
	}

	public void apply() {
		AlphaTestStorage.overrideAlphaTest(this.alphaTest);
	}

	public static void restore() {
		AlphaTestStorage.restoreAlphaTest();
	}
}
