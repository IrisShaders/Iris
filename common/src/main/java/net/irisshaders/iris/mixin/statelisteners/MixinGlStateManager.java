package net.irisshaders.iris.mixin.statelisteners;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Unique
	private static Runnable blendFuncListener;

	static {
		StateUpdateNotifiers.blendFuncNotifier = listener -> blendFuncListener = listener;
	}

	@Inject(method = "_blendFuncSeparate", at = @At("RETURN"), remap = false)
	private static void iris$onBlendFunc(int i, int j, int k, int l, CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
		}
	}
}
