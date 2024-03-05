package net.irisshaders.iris.mixin.sky;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables void plane rendering when submerged in a fluid, to avoid breaking the fog illusion in oceans
 * and lava.
 * <p>
 * Inspired by <a href="https://github.com/CaffeineMC/sodium-fabric/pull/710">this Sodium PR</a>, but this implementation
 * takes a far more conservative approach and only disables specific parts of sky rendering in high-fog
 * situations.
 */
@Mixin(ClientLevel.ClientLevelData.class)
public class MixinClientLevelData_DisableVoidPlane {
	@Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
	private void iris$getHorizonHeight(CallbackInfoReturnable<Double> cir) {
		FogType fogType = Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera();

		if (fogType != FogType.NONE) {
			cir.setReturnValue(Double.NEGATIVE_INFINITY);
		}
	}
}
