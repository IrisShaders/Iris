package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilderMeshingTask.class)
public class MixinChunkMeshBuildTask {
	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"))
	private void iris$onRenderModel(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		((BlockSensitiveBufferBuilder) buffers).beginBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockState), (byte) 0, (byte) blockState.getLightEmission(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"))
	private void iris$onRenderLiquid(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState, @Local FluidState fluidState, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		((BlockSensitiveBufferBuilder) buffers).beginBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(fluidState.createLegacyBlock()), (byte) 1, (byte) blockState.getLightEmission(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolidRender(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
	private void iris$onEnd(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState) {
		((BlockSensitiveBufferBuilder) buffers).endBlock();
	}
}
