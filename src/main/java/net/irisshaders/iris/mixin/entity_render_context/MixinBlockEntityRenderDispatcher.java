package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.batchedentityrendering.impl.Groupable;
import net.irisshaders.batchedentityrendering.impl.wrappers.TaggingRenderTypeWrapper;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.BufferSourceWrapper;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps block entity rendering functions in order to create additional render layers
 * that provide context to shaders about what block entity is currently being
 * rendered.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	private static final String RUN_REPORTED =
		"Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Runnable;)V";

	// I inject here in the method so that:
	//
	// 1. we can know that some checks we need have already been done
	// 2. if someone cancels this method hopefully it gets cancelled before this point, so we
	//    aren't running any redundant computations.
	//
	// NOTE: This is the last location that we can inject at, because the MultiBufferSource variable gets
	// captured by the lambda shortly afterwards, and therefore our ModifyVariable call becomes ineffective!
	@ModifyVariable(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/state/BlockState;)Z"),
		allow = 1, require = 1, argsOnly = true)
	private MultiBufferSource iris$wrapBufferSource(MultiBufferSource bufferSource, BlockEntity blockEntity) {
		BlockState state = blockEntity.getBlockState();

		Object2IntMap<BlockState> blockStateIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();

		if (blockStateIds == null) {
			return bufferSource;
		}

		int intId = blockStateIds.getOrDefault(state, -1);

		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(intId);

		return new BufferSourceWrapper(bufferSource, (renderType) -> OuterWrappedRenderType.wrapExactlyOnce("iris:block_entity", renderType, BlockEntityRenderStateShard.INSTANCE));
	}


	@Inject(method = "render", at = @At(value = "INVOKE", target = RUN_REPORTED, shift = At.Shift.AFTER))
	private void iris$afterRender(BlockEntity blockEntity, float tickDelta, PoseStack matrix,
								  MultiBufferSource bufferSource, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
