package net.coderbot.iris.rendertarget;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.util.Objects;
import java.util.Random;

public class NativeImageBackedNoiseTexture extends NativeImageBackedTexture {
	public NativeImageBackedNoiseTexture(int size) {
		super(create(size));
	}

	private static NativeImage create(int size) {
		NativeImage image = new NativeImage(NativeImage.Format.ABGR, size, size, false);
		Random random = new Random(0);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int color = random.nextInt() | (255 << 24);

				image.setPixelColor(x, y, color);
			}
		}

		return image;
	}

	@Override
	public void upload() {
		NativeImage image = Objects.requireNonNull(getImage());

		bindTexture();
		image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), true, false, false, false);
	}
}
