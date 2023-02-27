package net.coderbot.iris.texture.mipmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor;

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
		return FastColor.ABGR32.color(
				alphaFunc.blend(
					FastColor.ABGR32.alpha(c0),
					FastColor.ABGR32.alpha(c1),
					FastColor.ABGR32.alpha(c2),
					FastColor.ABGR32.alpha(c3)
				),
				blueFunc.blend(
					FastColor.ABGR32.blue(c0),
					FastColor.ABGR32.blue(c1),
					FastColor.ABGR32.blue(c2),
					FastColor.ABGR32.blue(c3)
				),
				greenFunc.blend(
					FastColor.ABGR32.green(c0),
					FastColor.ABGR32.green(c1),
					FastColor.ABGR32.green(c2),
					FastColor.ABGR32.green(c3)
				),
				redFunc.blend(
					FastColor.ABGR32.red(c0),
					FastColor.ABGR32.red(c1),
					FastColor.ABGR32.red(c2),
					FastColor.ABGR32.red(c3)
				)
		);
	}

	public interface BlendFunction {
		int blend(int v0, int v1, int v2, int v3);
	}
}
