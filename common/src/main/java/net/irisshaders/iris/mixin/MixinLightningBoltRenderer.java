package net.irisshaders.iris.mixin;

import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningBoltRenderer.class)
public class MixinLightningBoltRenderer {
	//@Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/LightningBoltRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;lightning()Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType iris$overrideTex() {
		//return LightningHandler.IRIS_LIGHTNING;
		return null;
	}
}
