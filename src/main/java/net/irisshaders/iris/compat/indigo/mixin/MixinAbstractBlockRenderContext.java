package net.irisshaders.iris.compat.indigo.mixin;

import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Small tweak to Indigo to make it obey our separateAo setting.
 */
@Mixin(targets = "net/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext", remap = false)
@Pseudo
@SuppressWarnings("all")
public class MixinAbstractBlockRenderContext {
	// Copied from ColorHelper from Indigo, licensed under the Apache v2 license.
	private static int iris$multiplyRGB(int color, float shade) {
		final int alpha = ((color >> 24) & 0xFF);
		final int red = (int) (((color >> 16) & 0xFF) * shade);
		final int green = (int) (((color >> 8) & 0xFF) * shade);
		final int blue = (int) ((color & 0xFF) * shade);

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	// One of these injections must pass, or else the game will crash.
	@Redirect(method = {"shadeQuad", "shadeFlatQuad"},
		at = @At(value = "INVOKE",
			target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/helper/ColorHelper;multiplyRGB(IF)I"), require = 0)
	private int iris$separateAoColorMultiply(int color, float ao) {
		if (WorldRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			color &= 0x00FFFFFF;
			color |= ((int) (ao * 255.0f)) << 24;

			return color;
		} else {
			return iris$multiplyRGB(color, ao);
		}
	}
}
