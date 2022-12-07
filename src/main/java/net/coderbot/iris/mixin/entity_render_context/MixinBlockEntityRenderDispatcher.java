package net.coderbot.iris.mixin.entity_render_context;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.layer.BlockEntityRenderStateShard;
import net.coderbot.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Wraps block entity rendering functions in order to create additional render layers
 * that provide context to shaders about what block entity is currently being
 * rendered.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

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
		allow = 1, require = 1)
	private <E extends BlockEntity> MultiBufferSource iris$wrapBufferSource(MultiBufferSource bufferSource, BlockEntity blockEntity) {
		if (!(bufferSource instanceof Groupable)) {
			// Fully batched entity rendering is not being used, do not use this wrapper!!!
			return bufferSource;
		}

		Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

		if (blockStateIds == null ) {
			return bufferSource;
		}

		// At this point, based on where we are in BlockEntityRenderDispatcher:
		// - The block entity is non-null
		// - The block entity has a world
		// - The block entity is not sure that it's supported by a valid block

		BlockState state = blockEntity.getBlockState();
		if (!blockEntity.getType().isValid(state)) {
			return bufferSource;
		}

		int intId = blockStateIds.getOrDefault(state, -1);
		RenderStateShard stateShard = BlockEntityRenderStateShard.forId(intId);

		return type ->
			bufferSource.getBuffer(OuterWrappedRenderType.wrapExactlyOnce("iris:is_block_entity", type, stateShard));
	}
}
