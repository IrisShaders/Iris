package net.coderbot.iris.mixin.shadows;

import net.coderbot.iris.shadows.ShadowRenderingState;
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
	@Inject(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;FJII[F)V",
	        at = @At("HEAD"), cancellable = true)
	private static void iris$noLightBeamInShadowPass(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
													 float tickDelta, long worldTime, int yOffset, int maxY, float[] color,
													 CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Don't do this if we're doing the "Unified Entity Rendering" optimization
			// TODO: This isn't necessary on most shaderpacks if we support blockEntityId
			ci.cancel();
		}
	}
}
