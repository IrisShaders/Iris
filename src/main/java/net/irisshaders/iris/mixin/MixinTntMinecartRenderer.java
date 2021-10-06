package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.batchedentityrendering.impl.Groupable;
import net.irisshaders.iris.layer.EntityColorRenderStateShard;
import net.irisshaders.iris.layer.EntityColorMultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartRenderer.class)
public abstract class MixinTntMinecartRenderer {
	@ModifyVariable(method = "renderWhiteSolidBlock", at = @At("HEAD"))
	private static MultiBufferSource iris$wrapProvider(MultiBufferSource bufferSource, BlockState blockState,
													   PoseStack poseStack, MultiBufferSource bufferSourceArg, int light,
													   boolean drawFlash) {
		if (!(bufferSource instanceof Groupable)) {
			// Entity color is not supported in this context, no buffering available.
			return bufferSource;
		}

		if (drawFlash) {
			EntityColorRenderStateShard phase = new EntityColorRenderStateShard(false, 1.0F);
			return new EntityColorMultiBufferSource(bufferSource, phase);
		} else {
			return bufferSource;
		}
	}
}
