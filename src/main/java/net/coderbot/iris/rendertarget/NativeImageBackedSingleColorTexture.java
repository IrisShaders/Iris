package net.coderbot.iris.rendertarget;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class NativeImageBackedSingleColorTexture extends NativeImageBackedTexture {
	public NativeImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(create(NativeImage.getAbgrColor(alpha, blue, green, red)));
	}

	private static NativeImage create(int color) {
		NativeImage image = new NativeImage(NativeImage.Format.ABGR, 1, 1, false);

		image.setPixelColor(0, 0, color);

		return image;
	}
}
