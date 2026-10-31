package net.irisshaders.iris.mixin;

import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinRenderType {
	@Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V"))
	private void redirectSetupRenderState(RenderType renderType) {
		if (!shouldSkipRenderState(renderType)) {
			renderType.setupRenderState();
		}
	}
	@Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V"))
	private void redirectClearRenderState(RenderType renderType) {
		if (!shouldSkipRenderState(renderType)) {
			renderType.clearRenderState();
		}
	}

	private static boolean shouldSkipRenderState(RenderType renderType) {
		return ImmediateState.mergeRendering && ImmediateState.mergedRenderType == renderType;
	}
}
