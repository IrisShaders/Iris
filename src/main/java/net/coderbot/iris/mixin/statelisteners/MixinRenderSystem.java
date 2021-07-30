package net.coderbot.iris.mixin.statelisteners;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
	private static Runnable fogDensityListener;

	@Inject(method = "setShaderFogEnd", at = @At("TAIL"), remap = false)
	private static void iris$onFogDensity(float f, CallbackInfo ci) {
		if (fogDensityListener != null) {
			fogDensityListener.run();
		}
	}

	static {
		StateUpdateNotifiers.fogDensityNotifier = listener -> fogDensityListener = listener;
	}
}
