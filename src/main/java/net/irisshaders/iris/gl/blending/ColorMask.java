package net.irisshaders.iris.gl.blending;

public class ColorMask {
	private final boolean red;
	private final boolean green;
	private final boolean blue;
	private final boolean alpha;

	public ColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public boolean isRedMasked() {
		return red;
	}

	public boolean isGreenMasked() {
		return green;
	}

	public boolean isBlueMasked() {
		return blue;
	}

	public boolean isAlphaMasked() {
		return alpha;
	}
}
