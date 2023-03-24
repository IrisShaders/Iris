package net.coderbot.iris.mixin.bettermipmaps;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.helpers.ColorSRGB;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Locale;
import java.util.Objects;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite {
	// Generate some color tables for gamma correction.
	private static final float[] SRGB_TO_LINEAR = new float[256];

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
	@ModifyVariable(method = "<init>", at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;animatedTexture:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$AnimatedTexture;"), argsOnly = true)
	private NativeImage iris$beforeGenerateMipLevels(NativeImage nativeImage, TextureAtlas arg, TextureAtlasSprite.Info info) {
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
	private static void iris$fillInTransparentPixelColors(NativeImage nativeImage) {
		final long ppPixel = getPointerRGBA(nativeImage);
		final int pixelCount = nativeImage.getHeight() * nativeImage.getWidth();

		// Calculate an average color from all pixels that are not completely transparent.
		// This average is weighted based on the (non-zero) alpha value of the pixel.
		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;

		float totalWeight = 0.0f;

		for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
			long pPixel = ppPixel + (pixelIndex * 4);

			int color = MemoryUtil.memGetInt(pPixel);
			int alpha = NativeImage.getA(color);

			// Ignore all fully-transparent pixels for the purposes of computing an average color.
			if (alpha != 0) {
				float weight = (float) alpha;

				// Make sure to convert to linear space so that we don't lose brightness.
				r += ColorSRGB.srgbToLinear(NativeImage.getR(color)) * weight;
				g += ColorSRGB.srgbToLinear(NativeImage.getG(color)) * weight;
				b += ColorSRGB.srgbToLinear(NativeImage.getB(color)) * weight;

				totalWeight += weight;
			}
		}

		// Bail if none of the pixels are semi-transparent.
		if (totalWeight == 0.0f) {
			return;
		}

		r /= totalWeight;
		g /= totalWeight;
		b /= totalWeight;

		// Convert that color in linear space back to sRGB.
		// Use an alpha value of zero - this works since we only replace pixels with an alpha value of 0.
		int averageColor = ColorSRGB.linearToSrgb(r, g, b, 0);

		for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
			long pPixel = ppPixel + (pixelIndex * 4);

			int color = MemoryUtil.memGetInt(pPixel);
			int alpha = NativeImage.getA(color);

			// Replace the color values of pixels which are fully transparent, since they have no color data.
			if (alpha == 0) {
				MemoryUtil.memPutInt(pPixel, averageColor);
			}
		}
	}

	private static long getPointerRGBA(NativeImage nativeImage) {
		if (nativeImage.format() != NativeImage.Format.RGBA) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
				"Tried to get pointer to RGBA pixel data on NativeImage of wrong format; have %s", nativeImage.format()));
		}

		return nativeImage.pixels;
	}
}
