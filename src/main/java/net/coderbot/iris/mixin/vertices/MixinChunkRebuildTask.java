package net.coderbot.iris.mixin.vertices;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public class MixinChunkRebuildTask {
	@Unique
	private BlockSensitiveBufferBuilder lastBufferBuilder;

	@Unique
	private IdMap idMap = getIdMap();

	@Unique
	private IdMap getIdMap() {
		ShaderPack pack = Iris.getCurrentPack();

		if (pack == null) {
			return null;
		}

		return pack.getIdMap();
	}

	@Unique
	private short resolveBlockId(BlockState state) {
		if (idMap == null) {
			return -1;
		}

		// TODO: This is not thread safe! It could crash during shader reloads...
		Identifier id = Registry.BLOCK.getId(state.getBlock());
		return (short) (int) idMap.getBlockProperties().getOrDefault(id, -1);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderFluid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/fluid/FluidState;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$onRenderFluid(float cameraX, float cameraY, float cameraZ, ChunkBuilder.ChunkData data, BlockBufferBuilderStorage buffers, CallbackInfoReturnable<Set> cir, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set set, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, FluidState fluidState, RenderLayer renderLayer, BufferBuilder bufferBuilder) {
		if (bufferBuilder instanceof BlockSensitiveBufferBuilder) {
			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder);
			lastBufferBuilder.beginBlock(resolveBlockId(fluidState.getBlockState()));
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderFluid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/fluid/FluidState;)Z", shift = At.Shift.AFTER))
	private void iris$finishRenderingFluid(float cameraX, float cameraY, float cameraZ, ChunkBuilder.ChunkData data, BlockBufferBuilderStorage buffers, CallbackInfoReturnable<Set> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$onRenderBlock(float cameraX, float cameraY, float cameraZ, ChunkBuilder.ChunkData data, BlockBufferBuilderStorage buffers, CallbackInfoReturnable<Set> cir, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set set, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, RenderLayer renderLayer2, BufferBuilder bufferBuilder) {
		if (bufferBuilder instanceof BlockSensitiveBufferBuilder) {
			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder);
			lastBufferBuilder.beginBlock(resolveBlockId(blockState));
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;)Z", shift = At.Shift.AFTER))
	private void iris$finishRenderingBlock(float cameraX, float cameraY, float cameraZ, ChunkBuilder.ChunkData data, BlockBufferBuilderStorage buffers, CallbackInfoReturnable<Set> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}
}
