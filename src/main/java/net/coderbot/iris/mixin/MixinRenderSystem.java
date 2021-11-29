package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
	@Inject(method = "initRenderer", at = @At("RETURN"))
	private static void iris$onRendererInit(int debugVerbosity, boolean alwaysFalse, CallbackInfo ci) {
		Iris.onRenderSystemInit();
	}
}
