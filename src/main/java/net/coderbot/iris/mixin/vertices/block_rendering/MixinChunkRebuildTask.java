package net.coderbot.iris.mixin.vertices.block_rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask", priority = 999)
public class MixinChunkRebuildTask {
	private static final String RENDER = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask;compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;";

	@Unique
	private BlockSensitiveBufferBuilder lastBufferBuilder;

	// Resolve the ID map on the main thread to avoid thread safety issues
	@Unique
	private final Object2IntMap<BlockState> blockStateIds = getBlockStateIds();

	@Unique
	private Object2IntMap<BlockState> getBlockStateIds() {
		return BlockRenderingSettings.INSTANCE.getBlockStateIds();
	}

	@Unique
	private short resolveBlockId(BlockState state) {
		if (blockStateIds == null) {
			return -1;
		}

		return (short) blockStateIds.getOrDefault(state, -1);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$onRenderLiquid(float cameraX, float cameraY, float cameraZ, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir, ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults results,int i, BlockPos blockPos, BlockPos blockPos2, VisGraph chunkOcclusionDataBuilder,RenderChunkRegion chunkRendererRegion, PoseStack poseStack, Set set2, RandomSource random, BlockRenderDispatcher blockRenderManager, Iterator<BlockPos> var15, BlockPos blockPos3, BlockState blockState, BlockState state2, FluidState fluidState, RenderType renderType, BufferBuilder bufferBuilder2) {
		if (bufferBuilder2 instanceof BlockSensitiveBufferBuilder) {
			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder2);
			// All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
			// TODO: We're using createLegacyBlock? That seems like something that Mojang wants to deprecate.
			lastBufferBuilder.beginBlock(resolveBlockId(fluidState.createLegacyBlock()), ExtendedDataHelper.FLUID_RENDER_TYPE, blockPos3.getX() & 0xF, blockPos3.getY() & 0xF, blockPos3.getZ() & 0xF);
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V", shift = At.Shift.AFTER))
	private void iris$finishRenderingLiquid(float cameraX, float cameraY, float cameraZ, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$onRenderBlock(float cameraX, float cameraY, float cameraZ, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir, ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults results, int i, BlockPos blockPos, BlockPos blockPos2, VisGraph chunkOcclusionDataBuilder,RenderChunkRegion chunkRendererRegion, PoseStack poseStack, Set set2, RandomSource random, BlockRenderDispatcher blockRenderManager, Iterator<BlockPos> var15, BlockPos blockPos3, BlockState blockState, BlockState state2, FluidState fluidState, RenderType renderType2, BufferBuilder bufferBuilder2) {
		if (bufferBuilder2 instanceof BlockSensitiveBufferBuilder) {
			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder2);
			// TODO: Resolve render types for normal blocks?
			lastBufferBuilder.beginBlock(resolveBlockId(blockState), ExtendedDataHelper.BLOCK_RENDER_TYPE, blockPos3.getX() & 0xF, blockPos3.getY() & 0xF, blockPos3.getZ() & 0xF);
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V", shift = At.Shift.AFTER))
	private void iris$finishRenderingBlock(float cameraX, float cameraY, float cameraZ, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}
}
