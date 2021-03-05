package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BakedQuad.class)
public class MixinBakedQuad {
	@Inject(method = "hasShade()Z", at = @At("HEAD"), cancellable = true)
	private void hasShade(CallbackInfoReturnable<Boolean> cir) {
		if (Iris.shouldDisableDirectionalShading()) {
			cir.setReturnValue(false);
		}
	}
}
