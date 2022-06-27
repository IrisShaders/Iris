package net.coderbot.iris.compat.sodium.mixin.separate_ao;

import net.caffeinemc.sodium.util.packed.ColorABGR;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ColorABGR.class)
public abstract class MixinColorABGR {
	@Shadow
	public static int mul(int color, float w) {
		throw new AssertionError();
	}

	@Inject(method = "repack", at = @At("HEAD"), cancellable = true, remap = false)
	private static void iris$separateAo(int rgb, float alpha, CallbackInfoReturnable<Integer> cir) {
		if (!BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			cir.setReturnValue(mul(rgb, alpha));
		}
	}
}
