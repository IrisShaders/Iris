package net.irisshaders.iris.compat.dh.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.LodRendererEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.seibel.distanthorizons.core.api.internal.ClientApi", remap = false)
public class MixinClientApi {
	@Inject(method = {"renderLods", "renderDeferredLodsForShaders"}, at = @At("HEAD"), cancellable = true)
	private void iris$renderOutsideWorldPass(CallbackInfo ci) {
		if (Iris.isPackInUseQuick() && !LodRendererEvents.isRenderingLods()) {
			ci.cancel();
		}
	}
}
