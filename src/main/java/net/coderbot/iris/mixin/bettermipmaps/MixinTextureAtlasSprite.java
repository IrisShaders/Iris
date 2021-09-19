package net.coderbot.iris.mixin.bettermipmaps;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite {
	// Generate some color tables for gamma correction.
	private static final float[] SRGB_TO_LINEAR = new float[256];

	static {
		for (int i = 0; i < 256; i++) {
			SRGB_TO_LINEAR[i] = (float) Math.pow(i / 255.0, 2.2);
		}
	}

	/**
	 * Fixes a common issue in image editing programs where fully transparent pixels are saved with fully black colors.
	 *
	 * This causes issues with mipmapped texture filtering, since the black color is used to calculate the final color
	 * even though the alpha value is zero. While ideally it would be disregarded, we do not control that. Instead,
	 * this code tries to calculate a decent average color to assign to these fully-transparent pixels so that their
	 * black color does not leak over into sampling.
	 */
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels(Lcom/mojang/blaze3d/platform/NativeImage;I)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private void fillInTransparentPixelColors(TextureAtlas textureAtlas, TextureAtlasSprite.Info info, int i, int j, int k, int l, int m, NativeImage nativeImage, CallbackInfo ci) {
		if (info.name().getPath().contains("leaves")) {
			// Don't ruin the textures of leaves on fast graphics, since they're supposed to have black pixels
			// apparently.
			return;
		}

		// Calculate an average color from all pixels that are not completely transparent.
		//
		// This average is weighted based on the (non-zero) alpha value of the pixel.
		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;
		float totalAlpha = 0.0f;

		for (int y = 0; y < nativeImage.getHeight(); y++) {
			for (int x = 0; x < nativeImage.getWidth(); x++) {
				int color = nativeImage.getPixelRGBA(x, y);
				int alpha = (color >> 24) & 255;

				if (alpha == 0) {
					// Ignore all fully-transparent pixels for the purposes of computing an average color.
					continue;
				}

				totalAlpha += alpha;

				// Make sure to convert to linear space so that we don't lose brightness.
				r += unpackLinearComponent(color, 0) * alpha;
				g += unpackLinearComponent(color, 8) * alpha;
				b += unpackLinearComponent(color, 16) * alpha;
			}
		}

		r /= totalAlpha;
		g /= totalAlpha;
		b /= totalAlpha;

		// If there weren't any pixels that were not fully transparent, bail out.
		if (totalAlpha == 0.0f) {
			return;
		}

		// Convert that color in linear space back to sRGB.
		// Use an alpha value of zero - this works since we only replace pixels with an alpha value of 0.
		int resultColor = packLinearToSrgb(r, g, b);

		for (int y = 0; y < nativeImage.getHeight(); y++) {
			for (int x = 0; x < nativeImage.getWidth(); x++) {
				int color = nativeImage.getPixelRGBA(x, y);
				int alpha = (color >> 24) & 255;

				// If this pixel has nonzero alpha, don't touch it.
				if (alpha > 0) {
					continue;
				}

				// Replace the color values of this pixel with the average colors.
				nativeImage.setPixelRGBA(x, y, resultColor);
			}
		}
	}

	// Unpacks a single color component into linear color space from sRGB.
	@Unique
	private static float unpackLinearComponent(int color, int shift) {
		return SRGB_TO_LINEAR[(color >> shift) & 255];
	}

	// Packs 3 color components into sRGB from linear color space.
	@Unique
	private static int packLinearToSrgb(float r, float g, float b) {
		int srgbR = (int) (Math.pow(r, 1.0 / 2.2) * 255.0);
		int srgbG = (int) (Math.pow(g, 1.0 / 2.2) * 255.0);
		int srgbB = (int) (Math.pow(b, 1.0 / 2.2) * 255.0);

		return (srgbB << 16) | (srgbG << 8) | srgbR;
	}
}
