package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "handleDebugKeys", at = @At("RETURN"), cancellable = true)
    private void iris$handleDebugKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (Iris.handleDebugKeys(event)) {
			cir.setReturnValue(true);
		}
    }
}
