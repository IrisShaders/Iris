package net.irisshaders.iris.targets.backed;

import com.mojang.blaze3d.platform.NativeImage;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class NativeImageBackedSingleColorTexture extends DynamicTexture {
	public NativeImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(() -> "Single color texture", create(1, 1, ColorARGB.pack(red, green, blue, alpha)));
	}

	public NativeImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}

	public NativeImageBackedSingleColorTexture(int width, int height, int red, int green, int blue, int alpha) {
		super(() -> "Single color texture", create(width, height, ColorARGB.pack(red, green, blue, alpha)));
	}

	private static NativeImage create(int width, int height, int color) {
		NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				image.setPixel(i, j, color);
			}
		}

		return image;
	}
}
