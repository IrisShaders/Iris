package net.irisshaders.iris.mixin.shadows;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
public class MixinBeaconRenderer {
	@Inject(method = "submitBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/resources/ResourceLocation;FFIIIFF)V",
		at = @At("HEAD"), cancellable = true)
	private static void iris$noLightBeamInShadowPass(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ResourceLocation resourceLocation, float f, float g, int i, int j, int k, float h, float l, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Don't do this if we're doing the "Unified Entity Rendering" optimization
			// TODO: This isn't necessary on most shaderpacks if we support blockEntityId
			ci.cancel();
		}
	}
}
