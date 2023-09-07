package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.gl.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadWinding;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Passes additional information indirectly to the vertex writer to support the mc_Entity and at_midBlock parts of the vertex format.
 */
@Mixin(ChunkRenderRebuildTask.class)
public class MixinChunkRenderRebuildTask {
	@Inject(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/world/level/block/state/BlockState.getRenderShape()" +
					"Lnet/minecraft/world/level/block/RenderShape;"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$setLocalPos(ChunkBuildContext context,
								  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir,
								  ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds, ChunkBuildBuffers buffers,
								  ChunkRenderCacheLocal cacheLocal,
								  WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
								  BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset,
								  int relY, int relZ, int relX, BlockState blockState) {
		if (BlockRenderingSettings.INSTANCE.shouldVoxelizeLightBlocks() && blockState.getBlock() instanceof LightBlock) {
			ChunkModelBuilder buildBuffers = buffers.get(RenderType.cutout());
			((ChunkBuildBuffersExt) buffers).iris$setLocalPos(0, 0, 0);
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(true);
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(blockState, (short) 0);

			int vertexStart = buildBuffers.getVertexSink().getVertexCount();

			for (int i = 0; i < 4; i++) {
				float x = (float) ((relX & 15)) + 0.25f;
				float y = (float) ((relY & 15)) + 0.25f;
				float z = (float) ((relZ & 15)) + 0.25f;
				float u = 0;
				float v = 0;
				int color = 0;
				int light = blockState.getLightEmission() << 4 | blockState.getLightEmission() << 20;
				bounds.addBlock(relX & 15, relY & 15, relZ & 15);

				buildBuffers.getVertexSink().writeVertex(x, y, z, color, u, v, light, buildBuffers.getChunkId());
			}
			buildBuffers.getIndexBufferBuilder(ModelQuadFacing.UNASSIGNED).add(vertexStart, ModelQuadWinding.CLOCKWISE);

			buildBuffers.getVertexSink().flush();
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(false);
			return;
		}

		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

	@Inject(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getChunkRenderType(" +
						"Lnet/minecraft/world/level/block/state/BlockState;" +
					")Lnet/minecraft/client/renderer/RenderType;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$wrapGetBlockLayer(ChunkBuildContext context,
										CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir,
										ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds, ChunkBuildBuffers buffers,
										ChunkRenderCacheLocal cacheLocal,
										WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
										BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset,
										int relY, int relZ, int relX, BlockState blockState) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE);
		}
	}

	@Inject(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getRenderLayer(" +
						"Lnet/minecraft/world/level/material/FluidState;" +
					")Lnet/minecraft/client/renderer/RenderType;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$wrapGetFluidLayer(ChunkBuildContext context,
										CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir,
										ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds, ChunkBuildBuffers buffers,
										ChunkRenderCacheLocal cacheLocal,
										WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
										BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset,
										int relY, int relZ, int relX, BlockState blockState, boolean rendered, FluidState fluidState) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setMaterialId(fluidState.createLegacyBlock(), ExtendedDataHelper.FLUID_RENDER_TYPE);
		}
	}

	@Inject(method = "performBuild",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
	private void iris$resetContext(ChunkBuildContext buildContext, CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$resetBlockContext();
		}
	}
}
