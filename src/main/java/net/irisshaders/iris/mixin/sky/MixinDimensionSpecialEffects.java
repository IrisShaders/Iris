package net.irisshaders.iris.mixin.sky;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables the sunrise / sunset effect when blindness is active or when submerged in a fluid.
 * <p>
 * Inspired by <a href="https://github.com/CaffeineMC/sodium-fabric/pull/710">this Sodium PR</a>, but this implementation
 * takes a far more conservative approach and only disables specific parts of sky rendering in high-fog
 * situations.
 */
@Mixin(DimensionSpecialEffects.class)
public class MixinDimensionSpecialEffects {
	@Inject(method = "getSunriseColor", at = @At("HEAD"), cancellable = true)
	private void iris$getSunriseColor(float timeOfDay, float partialTicks, CallbackInfoReturnable<float[]> cir) {
		Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
		boolean hasBlindness = cameraEntity instanceof LivingEntity
			&& ((LivingEntity) cameraEntity).hasEffect(MobEffects.BLINDNESS);

		if (hasBlindness) {
			cir.setReturnValue(null);
		}

		FogType fogType = Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera();

		if (fogType != FogType.NONE) {
			cir.setReturnValue(null);
		}
	}
}
