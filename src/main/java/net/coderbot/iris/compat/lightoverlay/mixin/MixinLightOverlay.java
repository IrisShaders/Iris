package net.coderbot.iris.compat.lightoverlay.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows us to figure out when Light Overlay is about to dispatch a draw call, since the 1.16.5 version of Light Overlay
 * does not use GlStateManager#drawArrays. This is required for us to update our state in time.
 *
 * This is the code path for when Light Overlay uses immediate mode without display lists. It draws numbers through
 * Minecraft's code (Font#drawInBatch), so that does not need special handling.
 */
@Pseudo
@Mixin(targets = "me/shedaniel/lightoverlay/common/fabric/LightOverlay")
public class MixinLightOverlay {
	@Inject(method = "lambda$register$2()V", at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL11.glBegin (I)V"))
	private static void iris$onGlBegin(CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::syncProgram);
	}
}
