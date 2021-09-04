package net.coderbot.iris.mixin;

import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.EntityColorVertexConsumerProvider;
import net.coderbot.iris.layer.InnerWrappedRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartEntityRenderer.class)
public abstract class MixinTntMinecartEntityRenderer {
	@ModifyVariable(method = "renderFlashingBlock", at = @At("HEAD"))
	private static VertexConsumerProvider iris$wrapProvider(VertexConsumerProvider provider, BlockState blockState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean drawFlash) {
		if (!(provider instanceof Groupable)) {
			// Entity color is not supported in this context, no buffering available.
			return provider;
		}

		if (drawFlash) {
			EntityColorRenderPhase phase = new EntityColorRenderPhase(false, 1.0F);
			return new EntityColorVertexConsumerProvider(provider, phase);
		} else {
			return provider;
		}
	}
}
