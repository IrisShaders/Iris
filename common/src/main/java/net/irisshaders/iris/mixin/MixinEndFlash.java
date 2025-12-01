package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndFlashState.class)
public class MixinEndFlash {
	@Shadow
	private float xAngle;

	@Shadow
	private float yAngle;

	@Shadow
	private long flashSeed;

	private static final float ABOVE_HORIZON_EPS = 1.0F; // degrees above horizon

	// Working on it...
	//@Inject(method = "calculateFlashParameters", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;randomBetween(Lnet/minecraft/util/RandomSource;FF)F", ordinal = 0), cancellable = true)
	private void iris$calculateNewAngles(long l, CallbackInfo ci, @Local(ordinal = 1) long m, @Local RandomSource randomSource) {
		if (Iris.getCurrentPack().isPresent()) {
			ci.cancel();
			this.xAngle = -Mth.randomBetween(randomSource, ABOVE_HORIZON_EPS, 60.0F); // [-60, -1]
			this.yAngle = Mth.randomBetween(randomSource, -180.0F, 180.0F);
			this.flashSeed = m;
		}
	}
}
