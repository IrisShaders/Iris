package net.coderbot.iris.gl.blending;

public class AlphaTest {
	private final int func;
	private final float reference;

	public AlphaTest(int func, float reference) {
		this.func = func;
		this.reference = reference;
	}

	public int getFunc() {
		return func;
	}

	public float getReference() {
		return reference;
	}
}
