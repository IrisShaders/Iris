package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(targets = "net.minecraft.client.renderer.state.level.QuadParticleRenderState$Storage")
public class MixinQuadParticleStorage {
	@Shadow
	private int capacity;
	@Shadow
	private int currentParticleIndex;
	@Unique
	private int[] iris$particleIds;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$allocate(CallbackInfo ci) {
		iris$particleIds = new int[capacity];
	}

	@Inject(method = "grow", at = @At("TAIL"))
	private void iris$grow(CallbackInfo ci) {
		iris$particleIds = Arrays.copyOf(iris$particleIds, capacity);
	}

	@Inject(method = "add", at = @At("TAIL"))
	private void iris$storeParticleId(CallbackInfo ci) {
		iris$particleIds[currentParticleIndex - 1] = CapturedRenderingState.INSTANCE.getCurrentRenderedParticle();
	}

	@Inject(method = "forEachParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$ParticleConsumer;consume(FFFFFFFFFFFFII)V"))
	private void iris$restoreParticleId(CallbackInfo ci, @Local(ordinal = 0) int index) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedParticle(iris$particleIds[index]);
	}

	@Inject(method = "forEachParticle", at = @At("RETURN"))
	private void iris$resetParticleId(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedParticle(0);
	}
}
