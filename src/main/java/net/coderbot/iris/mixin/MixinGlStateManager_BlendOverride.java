package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.blending.BlendModeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_BlendOverride {
	@Inject(method = {"_disableBlend", "_enableBlend"}, at = @At("HEAD"), cancellable = true)
	private static void blendToggleLock(CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			ci.cancel();
		}
	}

	@Inject(method = "_blendFunc", at = @At("HEAD"), cancellable = true)
	private static void blendFuncLock(int i, int j, CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			ci.cancel();
		}
	}

	@Inject(method = "_blendFuncSeparate", at = @At("HEAD"), cancellable = true)
	private static void blendFuncSeparateLock(int i, int j, int k, int l, CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			ci.cancel();
		}
	}
}
