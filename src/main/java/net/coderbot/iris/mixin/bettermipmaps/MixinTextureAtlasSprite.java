package net.coderbot.iris.mixin.bettermipmaps;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite {
	// Generate some color tables for gamma correction.
	private static final float[] SRGB_TO_LINEAR = new float[256];

	@Shadow
	@Final
	private TextureAtlasSprite.Info info;

	static {
		for (int i = 0; i < 256; i++) {
			SRGB_TO_LINEAR[i] = (float) Math.pow(i / 255.0, 2.2);
		}
	}

	// While Fabric allows us to @Inject into the constructor here, that's just a specific detail of FabricMC's mixin
	// fork. Upstream Mixin doesn't allow arbitrary @Inject usage in constructor. However, we can use @ModifyVariable
	// just fine, in a way that hopefully doesn't conflict with other mods.
	//
	// By doing this, we can work with upstream Mixin as well, as is used on Forge. While we don't officially
	// support Forge, since this works well on Fabric too, it's fine to ensure that the diff between Fabric and Forge
	// can remain minimal. Being less dependent on specific details of Fabric is good, since it means we can be more
	// cross-platform.
	@ModifyVariable(method = "<init>", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels(Lcom/mojang/blaze3d/platform/NativeImage;I)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private NativeImage iris$beforeGenerateMipLevels(NativeImage nativeImage) {
		// We're injecting after the "info" field has been set, so this is safe even though we're in a constructor.
		ResourceLocation name = Objects.requireNonNull(info).name();

		if (name.getPath().contains("leaves")) {
			// Don't ruin the textures of leaves on fast graphics, since they're supposed to have black pixels
			// apparently.
			return nativeImage;
		}

		iris$fillInTransparentPixelColors(nativeImage);

		return nativeImage;
	}

	/**
	 * Fixes a common issue in image editing programs where fully transparent pixels are saved with fully black colors.
	 *
	 * This causes issues with mipmapped texture filtering, since the black color is used to calculate the final color
	 * even though the alpha value is zero. While ideally it would be disregarded, we do not control that. Instead,
	 * this code tries to calculate a decent average color to assign to these fully-transparent pixels so that their
	 * black color does not leak over into sampling.
	 */
	@Unique
	private void iris$fillInTransparentPixelColors(NativeImage nativeImage) {
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
				r += iris$unpackLinearComponent(color, 0) * alpha;
				g += iris$unpackLinearComponent(color, 8) * alpha;
				b += iris$unpackLinearComponent(color, 16) * alpha;
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
		int resultColor = iris$packLinearToSrgb(r, g, b);

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
	private static float iris$unpackLinearComponent(int color, int shift) {
		return SRGB_TO_LINEAR[(color >> shift) & 255];
	}

	// Packs 3 color components into sRGB from linear color space.
	@Unique
	private static int iris$packLinearToSrgb(float r, float g, float b) {
		int srgbR = (int) (Math.pow(r, 1.0 / 2.2) * 255.0);
		int srgbG = (int) (Math.pow(g, 1.0 / 2.2) * 255.0);
		int srgbB = (int) (Math.pow(b, 1.0 / 2.2) * 255.0);

		return (srgbB << 16) | (srgbG << 8) | srgbR;
	}
}
