package net.coderbot.iris.texture.mipmap;

public class SpecularAlphaBlendFunction extends LinearBlendFunction {
	public static final SpecularAlphaBlendFunction INSTANCE = new SpecularAlphaBlendFunction();

	@Override
	public int blend(int v0, int v1, int v2, int v3) {
		if (v0 == 255) {
			v0 = 0;
		}
		if (v1 == 255) {
			v1 = 0;
		}
		if (v2 == 255) {
			v2 = 0;
		}
		if (v3 == 255) {
			v3 = 0;
		}
		return super.blend(v0, v1, v2, v3);
	}
}
