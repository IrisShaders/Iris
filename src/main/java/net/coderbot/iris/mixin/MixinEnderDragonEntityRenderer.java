package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntityRenderer.class)
public class MixinEnderDragonEntityRenderer {
	@Inject(method = "render", at = @At("HEAD"))
	private void setTicksSinceDeath(EnderDragonEntity enderDragonEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (enderDragonEntity.ticksSinceDeath != 0) {
			CapturedRenderingState.INSTANCE.setTicksSinceDragonDeath(enderDragonEntity.ticksSinceDeath);
		} else {
			CapturedRenderingState.INSTANCE.setTicksSinceDragonDeath(0);
		}
	}
}
