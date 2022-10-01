package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FishingHookRenderer.class)
public class MixinFishingHookRenderer {
	@Inject(method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
	private void capture(FishingHook arg, float f, float g, PoseStack arg2, MultiBufferSource arg3, int i, CallbackInfo ci) {
		VertexConsumer consumer = arg3.getBuffer(RenderType.lineStrip());
		// Create an "invisible vertex" with zero area. TODO: Find a proper fix, this isn't guaranteed under the spec but work on most drivers.
		consumer.vertex(0, 0, 0).color(0, 0, 0, 255).normal(0, 0, 0).endVertex();
	}
}
