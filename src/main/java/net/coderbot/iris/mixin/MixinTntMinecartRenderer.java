package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.layer.EntityColorRenderState;
import net.coderbot.iris.layer.InnerWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartRenderer.class)
public abstract class MixinTntMinecartRenderer {
	@ModifyVariable(method = "renderWhiteSolidBlock", at = @At("HEAD"))
	private static MultiBufferSource iris$wrapProvider(MultiBufferSource provider, BlockState blockState, PoseStack matrices, MultiBufferSource vertexConsumers, int light, boolean drawFlash) {
		if (drawFlash) {
			EntityColorRenderState phase = new EntityColorRenderState(false, 1.0F);
			return type -> provider.getBuffer(new InnerWrappedRenderType("iris_entity_color", type, phase));
		} else {
			return provider;
		}
	}
}
