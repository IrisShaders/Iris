package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilderMeshingTask.class)
public class MixinChunkMeshBuildTask {
	private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;", ordinal = 1))
	private void iris$setLightBlock(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		if (WorldRenderingSettings.INSTANCE.shouldVoxelizeLightBlocks() && blockState.getBlock() instanceof LightBlock) {
			ChunkModelBuilder buildBuffers = buffers.get(DefaultMaterials.CUTOUT);
			int id = WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockState);
			for (int i = 0; i < 4; i++) {
				// TODO: Add ignoreMidBlock support
				((ChunkVertexExtension) vertices[i]).iris$ignoresMidBlock(true);
				((ChunkVertexExtension) vertices[i]).iris$setData((byte) blockState.getLightEmission(), (byte) 0, id,
					(blockPos.getX() & 15), (blockPos.getY() & 15), (blockPos.getZ() & 15));
				vertices[i].x = (float) ((blockPos.getX() & 15)) + 0.25f;
				vertices[i].y = (float) ((blockPos.getY() & 15)) + 0.25f;
				vertices[i].z = (float) ((blockPos.getZ() & 15)) + 0.25f;
				vertices[i].u = 0;
				vertices[i].v = 0;
				vertices[i].color = 0;
				vertices[i].light = blockState.getLightEmission() << 4 | blockState.getLightEmission() << 20;
			}
			buildBuffers.getVertexBuffer(ModelQuadFacing.UNASSIGNED).push(vertices, DefaultMaterials.CUTOUT);
			//((BlockSensitiveBufferBuilder) buffers).ignoreMidBlock(false);
		}
	}

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"))
	private void iris$onRenderModel(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos, @Local BlockRenderer blockRenderer) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		((VertexEncoderInterface) blockRenderer).beginBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(blockState, -1), (byte) 0, (byte) blockState.getLightEmission(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"))
	private void iris$onRenderLiquid(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState, @Local FluidState fluidState, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos, @Local BlockRenderCache cache) {
		if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

		((VertexEncoderInterface) cache.getFluidRenderer()).beginBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(fluidState.createLegacyBlock()), (byte) 1, (byte) blockState.getLightEmission(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolidRender()Z"))
	private void iris$onEnd(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local ChunkBuildBuffers buffers, @Local BlockState blockState) {
		//((BlockSensitiveBufferBuilder) buffers).endBlock();
	}
}
