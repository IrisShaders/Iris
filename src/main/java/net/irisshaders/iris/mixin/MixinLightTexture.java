package net.irisshaders.iris.mixin;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class MixinLightTexture {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyDarken(F)F"))
	private void resetDarknessValue(float $$0, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setDarknessLightFactor(0.0F);
	}

	@Inject(method = "calculateDarknessScale", at = @At("RETURN"))
	private void storeDarknessValue(LivingEntity $$0, float $$1, float $$2, CallbackInfoReturnable<Float> cir) {
		CapturedRenderingState.INSTANCE.setDarknessLightFactor((float) (cir.getReturnValue() * this.minecraft.options.darknessEffectScale().get()));
	}
}
