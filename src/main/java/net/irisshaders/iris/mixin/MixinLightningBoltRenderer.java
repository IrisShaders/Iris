package net.irisshaders.iris.mixin;

import net.irisshaders.iris.pathways.LightningHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightningBoltRenderer.class)
public class MixinLightningBoltRenderer {
	@Redirect(method = "render(Lnet/minecraft/world/entity/LightningBolt;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;lightning()Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType iris$overrideTex() {
		return LightningHandler.IRIS_LIGHTNING;
	}
}
