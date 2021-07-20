package net.coderbot.iris.mixin;

import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.EntityColorWrappedRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartEntityRenderer.class)
public abstract class MixinTntMinecartEntityRenderer {
	@ModifyVariable(method = "renderFlashingBlock", at = @At("HEAD"))
	private static VertexConsumerProvider iris$wrapProvider(VertexConsumerProvider provider, BlockState blockState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean drawFlash) {
		if (drawFlash) {
			EntityColorRenderPhase phase = new EntityColorRenderPhase(false, 1.0F);
			return layer -> provider.getBuffer(new EntityColorWrappedRenderLayer("iris_entity_color", layer, phase));
		} else {
			return provider;
		}
	}
}
