package net.coderbot.iris.mixin.texture.pbr;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NativeImage.class)
public class MixinNativeImage {
	@Inject(method = "setFilter", at = @At("HEAD"), cancellable = true)
	private static void cancel(boolean bl, boolean bl2, CallbackInfo ci) {
		if (IrisRenderSystem.areParametersLocked()) {
			ci.cancel();
		}
	}
}
