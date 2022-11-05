package net.coderbot.iris.texture.mipmap;

import com.mojang.blaze3d.platform.NativeImage;

public abstract class AbstractMipmapGenerator implements CustomMipmapGenerator {
	@Override
	public NativeImage[] generateMipLevels(NativeImage image, int mipLevel) {
		NativeImage[] images = new NativeImage[mipLevel + 1];
		images[0] = image;
		if (mipLevel > 0) {
			for (int level = 1; level <= mipLevel; ++level) {
				NativeImage prevMipmap = images[level - 1];
				NativeImage mipmap = new NativeImage(prevMipmap.getWidth() >> 1, prevMipmap.getHeight() >> 1, false);
				int width = mipmap.getWidth();
				int height = mipmap.getHeight();
				for (int x = 0; x < width; ++x) {
					for (int y = 0; y < height; ++y) {
						mipmap.setPixelRGBA(x, y, blend(
								prevMipmap.getPixelRGBA(x * 2 + 0, y * 2 + 0),
								prevMipmap.getPixelRGBA(x * 2 + 1, y * 2 + 0),
								prevMipmap.getPixelRGBA(x * 2 + 0, y * 2 + 1),
								prevMipmap.getPixelRGBA(x * 2 + 1, y * 2 + 1)
						));
					}
				}
				images[level] = mipmap;
			}
		}
		return images;
	}

	public abstract int blend(int c0, int c1, int c2, int c3);
}
