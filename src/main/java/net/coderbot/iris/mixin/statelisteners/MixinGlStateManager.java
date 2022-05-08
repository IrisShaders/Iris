package net.coderbot.iris.mixin.statelisteners;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	private static Runnable fogToggleListener;
	private static Runnable fogModeListener;
	private static Runnable fogStartListener;
	private static Runnable fogEndListener;
	private static Runnable fogDensityListener;
	private static Runnable blendFuncListener;

	@Inject(method = { "_enableFog", "_disableFog()V" }, at = @At("RETURN"))
	private static void iris$onFogToggle(CallbackInfo ci) {
		if (fogToggleListener != null) {
			fogToggleListener.run();
		}
	}

	@Inject(method = "_fogMode(I)V", at = @At(value = "FIELD", target = "com/mojang/blaze3d/platform/GlStateManager$FogState.mode : I", shift = At.Shift.AFTER))
	private static void iris$onFogMode(int mode, CallbackInfo ci) {
		if (fogModeListener != null) {
			fogModeListener.run();
		}
	}

	@Inject(method = "_fogDensity(F)V", at = @At(value = "FIELD", target = "com/mojang/blaze3d/platform/GlStateManager$FogState.density : F", shift = At.Shift.AFTER))
	private static void iris$onFogDensity(float density, CallbackInfo ci) {
		if (fogDensityListener != null) {
			fogDensityListener.run();
		}
	}

	@Inject(method = "_fogStart(F)V", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/GlStateManager$FogState;start:F", shift = At.Shift.AFTER))
	private static void iris$onFogStart(float density, CallbackInfo ci) {
		if (fogStartListener != null) {
			fogStartListener.run();
		}
	}

	@Inject(method = "_fogEnd(F)V", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/GlStateManager$FogState;end:F", shift = At.Shift.AFTER))
	private static void iris$onFogEnd(float density, CallbackInfo ci) {
		if (fogEndListener != null) {
			fogEndListener.run();
		}
	}

	@Inject(method = "_blendFunc", at = @At("RETURN"))
	private static void iris$onBlendFunc(int srcRgb, int dstRgb, CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
		}
	}

	@Inject(method = "_blendFuncSeparate", at = @At("RETURN"))
	private static void iris$onBlendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
		}
	}

	static {
		StateUpdateNotifiers.fogToggleNotifier = listener -> fogToggleListener = listener;
		StateUpdateNotifiers.fogModeNotifier = listener -> fogModeListener = listener;
		StateUpdateNotifiers.fogStartNotifier = listener -> fogStartListener = listener;
		StateUpdateNotifiers.fogEndNotifier = listener -> fogEndListener = listener;
		StateUpdateNotifiers.fogDensityNotifier = listener -> fogDensityListener = listener;
		StateUpdateNotifiers.blendFuncNotifier = listener -> blendFuncListener = listener;
	}
}
