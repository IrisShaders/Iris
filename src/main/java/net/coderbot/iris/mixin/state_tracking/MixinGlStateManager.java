package net.coderbot.iris.mixin.state_tracking;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gbuffer_overrides.state.StateTracker;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.samplers.IrisSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Shadow
	private static int activeTexture;

	@Inject(method = "_enableTexture()V", at = @At("HEAD"))
	private static void iris$onEnableTexture(CallbackInfo ci) {
		if (activeTexture == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = true;
		} else if (activeTexture == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = true;
		} else if (activeTexture == IrisSamplers.OVERLAY_TEXTURE_UNIT) {
			StateTracker.INSTANCE.overlaySampler = true;
		} else {
			return;
		}

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "_disableTexture()V", at = @At("HEAD"))
	private static void iris$onDisableTexture(CallbackInfo ci) {
		if (activeTexture == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = false;
		} else if (activeTexture == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = false;
		} else if (activeTexture == IrisSamplers.OVERLAY_TEXTURE_UNIT) {
			StateTracker.INSTANCE.overlaySampler = false;
		} else {
			return;
		}

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "_drawArrays(III)V", at = @At("HEAD"))
	private static void iris$beforeDrawArrays(int mode, int first, int count, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::syncProgram);
	}
}
