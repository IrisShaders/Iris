package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.BlockModelRenderStateExtension;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelResolver.class)
public class BlockModelResolverMixin {
	@Inject(method = "update", at = @At("TAIL"))
	private void iris$setBlock(BlockModelRenderState state, BlockState blockState, BlockDisplayContext displayContext, CallbackInfo ci) {
		((BlockModelRenderStateExtension) state).setBlock(blockState);
	}
}
