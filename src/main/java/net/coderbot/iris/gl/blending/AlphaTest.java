package net.coderbot.iris.gl.blending;

public class AlphaTest {
	private final AlphaTestFunction function;
	private final float reference;

	public AlphaTest(AlphaTestFunction function, float reference) {
		this.function = function;
		this.reference = reference;
	}

	public AlphaTestFunction getFunction() {
		return function;
	}

	public float getReference() {
		return reference;
	}
}
