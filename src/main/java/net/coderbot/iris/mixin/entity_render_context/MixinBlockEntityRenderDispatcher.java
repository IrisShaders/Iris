package net.coderbot.iris.mixin.entity_render_context;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.fantastic.WrappingVertexConsumerProvider;
import net.coderbot.iris.layer.BlockEntityRenderPhase;
import net.coderbot.iris.layer.OuterWrappedRenderLayer;
import net.coderbot.iris.shaderpack.IdMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps block entity rendering functions in order to create additional render layers
 * that provide context to shaders about what block entity is currently being
 * rendered.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	private static final String RENDER =
			"render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V";

	private static final String RUN_REPORTED = "runReported(Lnet/minecraft/block/entity/BlockEntity;Ljava/lang/Runnable;)V";

	// I inject here in the method so that:
	//
	// 1. we can know that some checks we need have already been done
	// 2. if someone cancels this method hopefully it gets cancelled before this point
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RUN_REPORTED))
	private void iris$beforeRender(BlockEntity blockEntity, float tickDelta, MatrixStack matrix,
								   VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (!(vertexConsumers instanceof WrappingVertexConsumerProvider)) {
			return;
		}

		IdMap idMap = BlockRenderingSettings.INSTANCE.getIdMap();

		if (idMap == null) {
			return;
		}

		// At this point, based on where we are in BlockEntityRenderDispatcher:
		// - The block entity is non-null
		// - The block entity has a world
		// - The block entity thinks that it's supported by a valid block

		int intId = idMap.getBlockProperties().getOrDefault(blockEntity.getCachedState(), -1);
		RenderPhase phase = BlockEntityRenderPhase.forId(intId);

		((WrappingVertexConsumerProvider) vertexConsumers).pushWrappingFunction(layer ->
				new OuterWrappedRenderLayer("iris:is_block_entity", layer, phase));
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RUN_REPORTED, shift = At.Shift.AFTER))
	private void iris$afterRender(BlockEntity blockEntity, float tickDelta, MatrixStack matrix,
								  VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (!(vertexConsumers instanceof WrappingVertexConsumerProvider)) {
			return;
		}

		// This might not get called if we crash and something like NotEnoughCrashes tries
		// to act like nothing happened.
		//
		// Supporting that is hard so I decided to just ignore that for now.
		//
		// This might also not get called if a different mod cancels before runReported but
		// after my inject, but I placed my inject there in the hopes that it would
		// not be likely to be affected by a cancel. I hope that the universe doesn't
		// conspire against me and cause that to break.
		((WrappingVertexConsumerProvider) vertexConsumers).popWrappingFunction();
	}
}
