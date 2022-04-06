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
	private static Runnable blendFuncListener;
	private static Runnable fogStartListener;
	private static Runnable fogEndListener;

	@Inject(method = "_blendFunc", at = @At("RETURN"), remap = false)
	private static void iris$onBlendFunc(int srcRgb, int dstRgb, CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
		}
	}

	@Inject(method = "_blendFuncSeparate", at = @At("RETURN"), remap = false)
	private static void iris$onBlendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
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

	static {
		StateUpdateNotifiers.fogStartNotifier = listener -> fogStartListener = listener;
		StateUpdateNotifiers.fogEndNotifier = listener -> fogEndListener = listener;
		StateUpdateNotifiers.blendFuncNotifier = listener -> blendFuncListener = listener;
	}
}
