package net.irisshaders.iris.pbr.mipmap;

import com.mojang.blaze3d.platform.NativeImage;

public abstract class AbstractMipmapGenerator implements CustomMipmapGenerator {
	@Override
	public NativeImage[] generateMipLevels(NativeImage[] image, int mipLevel) {
		if (mipLevel + 1 <= image.length) {
			return image;
		}

		NativeImage[] newImages = new NativeImage[mipLevel + 1];

		if (mipLevel > 0) {
			for (int level = 1; level <= mipLevel; ++level) {
				NativeImage prevMipmap = level == 1 ? image[0] : newImages[level - 1];
				NativeImage mipmap = new NativeImage(prevMipmap.getWidth() >> 1, prevMipmap.getHeight() >> 1, false);
				int width = mipmap.getWidth();
				int height = mipmap.getHeight();
				for (int x = 0; x < width; ++x) {
					for (int y = 0; y < height; ++y) {
						mipmap.setPixel(x, y, blend(
							prevMipmap.getPixel(x * 2, y * 2),
							prevMipmap.getPixel(x * 2 + 1, y * 2),
							prevMipmap.getPixel(x * 2, y * 2 + 1),
							prevMipmap.getPixel(x * 2 + 1, y * 2 + 1)
						));
					}
				}
				newImages[level] = mipmap;
			}
		}

		newImages[0] = image[0];

		return newImages;
	}

	public abstract int blend(int c0, int c1, int c2, int c3);
}
