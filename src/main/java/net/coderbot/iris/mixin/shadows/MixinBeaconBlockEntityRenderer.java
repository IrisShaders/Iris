package net.coderbot.iris.mixin.shadows;

import net.coderbot.iris.shadows.ShadowRenderingStatus;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconBlockEntityRenderer {
	@Inject(method = "renderLightBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/Identifier;FFJII[FFF)V",
	        at = @At("HEAD"), cancellable = true)
	private static void iris$noLightBeamInShadowPass(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
													 Identifier identifier, float f, float g, long l, int i, int j,
													 float[] fs, float h, float k, CallbackInfo ci) {
		if (ShadowRenderingStatus.areShadowsCurrentlyBeingRendered()) {
			// TODO: Don't do this if we're doing the "Unified Entity Rendering" optimization
			// TODO: This isn't necessary on most shaderpacks if we support blockEntityId
			ci.cancel();
		}
	}
}
