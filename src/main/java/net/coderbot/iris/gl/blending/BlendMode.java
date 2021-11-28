package net.coderbot.iris.gl.blending;

public class BlendMode {
	public int srcRgb;
	public int dstRgb;
	public int srcAlpha;
	public int dstAlpha;

	public BlendMode(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
		this.srcRgb = srcRgb;
		this.dstRgb = dstRgb;
		this.srcAlpha = srcAlpha;
		this.dstAlpha = dstAlpha;
	}
}
