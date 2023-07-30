package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Passes additional information indirectly to the vertex writer to support the mc_Entity and at_midBlock parts of the vertex format.
 */
@Mixin(ChunkRenderRebuildTask.class)
public class MixinChunkRenderRebuildTask {
	private ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

	@Inject(method = "performBuild", at = @At(value = "INVOKE",
		target = "net/minecraft/world/level/block/state/BlockState.getRenderShape()" +
			"Lnet/minecraft/world/level/block/RenderShape;"),
		locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$setLocalPos(ChunkBuildContext context,
								  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir,
								  BuiltSectionInfo.Builder renderData, VisGraph occluder, ChunkBuildBuffers buffers,
								  BlockRenderCache cacheLocal,
								  WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
								  BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset, BlockRenderContext context2,
								  int relY, int relZ, int relX, BlockState blockState) {
		if (BlockRenderingSettings.INSTANCE.shouldVoxelizeLightBlocks() && blockState.getBlock() instanceof LightBlock) {
			ChunkModelBuilder buildBuffers = buffers.get(DefaultMaterials.CUTOUT);
			((ChunkBuildBuffersExt) buffers).iris$setLocalPos(0, 0, 0);
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(true);
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(blockState, (short) 0);
			for (int i = 0; i < 4; i++) {
				vertices[i].x = (float) ((relX & 15)) + 0.25f;
				vertices[i].y = (float) ((relY & 15)) + 0.25f;
				vertices[i].z = (float) ((relZ & 15)) + 0.25f;
				vertices[i].u = 0;
				vertices[i].v = 0;
				vertices[i].color = 0;
				vertices[i].light = blockState.getLightEmission() << 4 | blockState.getLightEmission() << 20;
			}
			buildBuffers.getVertexBuffer(ModelQuadFacing.UNASSIGNED).push(vertices, DefaultMaterials.CUTOUT);
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(false);
			return;
		}

		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
	private BakedModel iris$wrapGetBlockLayer(BlockModelShaper instance, BlockState blockState, ChunkBuildContext context) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE);
		}

		return instance.getBlockModel(blockState);
	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"))
	private void iris$wrapGetFluidLayer(FluidRenderer instance, WorldSlice h2, FluidState fluidState, BlockPos h4, BlockPos north1, ChunkBuildBuffers buffers) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(fluidState.createLegacyBlock(), ExtendedDataHelper.FLUID_RENDER_TYPE);
		}

		instance.render(h2, fluidState, h4, north1, buffers);
	}

	@Inject(method = "performBuild",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
	private void iris$resetContext(ChunkBuildContext buildContext, CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$resetBlockContext();
		}
	}
}
