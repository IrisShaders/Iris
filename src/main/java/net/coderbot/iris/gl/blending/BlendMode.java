package net.coderbot.iris.gl.blending;

public class BlendMode {
	private final int srcRgb;
	private final int dstRgb;
	private final int srcAlpha;
	private final int dstAlpha;

	public BlendMode(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
		this.srcRgb = srcRgb;
		this.dstRgb = dstRgb;
		this.srcAlpha = srcAlpha;
		this.dstAlpha = dstAlpha;
	}

	public int getSrcRgb() {
		return srcRgb;
	}

	public int getDstRgb() {
		return dstRgb;
	}

	public int getSrcAlpha() {
		return srcAlpha;
	}

	public int getDstAlpha() {
		return dstAlpha;
	}
}
