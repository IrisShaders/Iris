package net.coderbot.iris.mixin.statelisteners;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	private static Runnable fogToggleListener;
	private static Runnable fogModeListener;
	private static Runnable fogDensityListener;

	//@Inject(method = { "enableFog()V", "disableFog()V" }, at = @At("RETURN"))
	private static void iris$onFogToggle(CallbackInfo ci) {
		if (fogToggleListener != null) {
			fogToggleListener.run();
		}
	}

	//@Inject(method = "fogMode(I)V", at = @At(value = "FIELD", target = "com/mojang/blaze3d/platform/GlStateManager$FogState.mode : I", shift = At.Shift.AFTER))
	private static void iris$onFogMode(int mode, CallbackInfo ci) {
		if (fogModeListener != null) {
			fogModeListener.run();
		}
	}

	//@Inject(method = "fogDensity(F)V", at = @At(value = "FIELD", target = "com/mojang/blaze3d/platform/GlStateManager$FogState.density : F", shift = At.Shift.AFTER))
	private static void iris$onFogDensity(float density, CallbackInfo ci) {
		if (fogDensityListener != null) {
			fogDensityListener.run();
		}
	}

	static {
		StateUpdateNotifiers.fogToggleNotifier = listener -> fogToggleListener = listener;
		StateUpdateNotifiers.fogModeNotifier = listener -> fogModeListener = listener;
		StateUpdateNotifiers.fogDensityNotifier = listener -> fogDensityListener = listener;
	}
}
