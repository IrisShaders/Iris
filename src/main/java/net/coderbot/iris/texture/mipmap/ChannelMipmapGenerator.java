package net.coderbot.iris.texture.mipmap;

import com.mojang.blaze3d.platform.NativeImage;

public class ChannelMipmapGenerator extends AbstractMipmapGenerator {
	protected final BlendFunction redFunc;
	protected final BlendFunction greenFunc;
	protected final BlendFunction blueFunc;
	protected final BlendFunction alphaFunc;

	public ChannelMipmapGenerator(BlendFunction redFunc, BlendFunction greenFunc, BlendFunction blueFunc, BlendFunction alphaFunc) {
		this.redFunc = redFunc;
		this.greenFunc = greenFunc;
		this.blueFunc = blueFunc;
		this.alphaFunc = alphaFunc;
	}

	@Override
	public int blend(int c0, int c1, int c2, int c3) {
		return NativeImage.combine(
				alphaFunc.blend(
						NativeImage.getA(c0),
						NativeImage.getA(c1),
						NativeImage.getA(c2),
						NativeImage.getA(c3)
				),
				blueFunc.blend(
						NativeImage.getB(c0),
						NativeImage.getB(c1),
						NativeImage.getB(c2),
						NativeImage.getB(c3)
				),
				greenFunc.blend(
						NativeImage.getG(c0),
						NativeImage.getG(c1),
						NativeImage.getG(c2),
						NativeImage.getG(c3)
				),
				redFunc.blend(
						NativeImage.getR(c0),
						NativeImage.getR(c1),
						NativeImage.getR(c2),
						NativeImage.getR(c3)
				)
		);
	}

	public interface BlendFunction {
		int blend(int v0, int v1, int v2, int v3);
	}
}
