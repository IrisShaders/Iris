package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.api.device.DeviceInfo;
import net.irisshaders.iris.Iris;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeviceInfo.class)
public class UndoReverseZOne {
	@Inject(method = "isZZeroToOne", at = @At("HEAD"), cancellable = true)
    private void iris$force(CallbackInfoReturnable<Boolean> cir) {
        if (Iris.isPackInUseQuick()) cir.setReturnValue(false);
    }
}
