package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.blending.AlphaTestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_AlphaTestOverride {
	//@Inject(method = "_disableAlphaTest", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaTestDisableLock(CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaTestToggle(false);
			ci.cancel();
		}
	}

	//@Inject(method = "_enableAlphaTest", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaTestEnableLock(CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaTestToggle(true);
			ci.cancel();
		}
	}

	//@Inject(method = "_alphaFunc", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaFuncLock(int function, float reference, CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaFunc(function, reference);
			ci.cancel();
		}
	}
}
