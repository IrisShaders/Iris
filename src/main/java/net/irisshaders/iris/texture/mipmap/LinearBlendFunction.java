package net.irisshaders.iris.texture.mipmap;

public class LinearBlendFunction implements ChannelMipmapGenerator.BlendFunction {
	public static final LinearBlendFunction INSTANCE = new LinearBlendFunction();

	@Override
	public int blend(int v0, int v1, int v2, int v3) {
		return (v0 + v1 + v2 + v3) / 4;
	}
}
