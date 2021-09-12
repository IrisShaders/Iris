package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.layer.EntityColorRenderStateShard;
import net.coderbot.iris.layer.EntityColorMultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartRenderer.class)
public abstract class MixinTntMinecartEntityRenderer {
	@ModifyVariable(method = "renderWhiteSolidBlock", at = @At("HEAD"))
	private static MultiBufferSource iris$wrapProvider(MultiBufferSource provider, BlockState blockState, PoseStack matrices, MultiBufferSource bufferSource, int light, boolean drawFlash) {
		if (!(provider instanceof Groupable)) {
			// Entity color is not supported in this context, no buffering available.
			return provider;
		}

		if (drawFlash) {
			EntityColorRenderStateShard phase = new EntityColorRenderStateShard(false, 1.0F);
			return new EntityColorMultiBufferSource(provider, phase);
		} else {
			return provider;
		}
	}
}
