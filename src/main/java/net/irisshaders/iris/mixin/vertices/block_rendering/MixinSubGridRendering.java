package net.irisshaders.iris.mixin.vertices.block_rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.grid.SubGridMeshBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;

/**
 * Captures and tracks the current block being rendered.
 * <p>
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(SubGridMeshBuilder.class)
public class MixinSubGridRendering {
	private static final String RENDER = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask;compile(FFFLnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask$CompileResults;";
	// Resolve the ID map on the main thread to avoid thread safety issues
	@Unique
	private final Object2IntMap<BlockState> blockStateIds = getBlockStateIds();

	@Unique
	private Object2IntMap<BlockState> getBlockStateIds() {
		return WorldRenderingSettings.INSTANCE.getBlockStateIds();
	}

	@Unique
	private short resolveBlockId(BlockState state) {
		if (blockStateIds == null) {
			return -1;
		}

		return (short) blockStateIds.getOrDefault(state, -1);
	}

	@WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;III)V"))
	private void iris$onRenderLiquid(BlockRenderDispatcher instance, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, int i, int j, int k, Operation<Void> original) {
		if (vertexConsumer instanceof BlockSensitiveBufferBuilder lastBufferBuilder) {
            // All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
			lastBufferBuilder.beginBlock(resolveBlockId(fluidState.createLegacyBlock()), ExtendedDataHelper.FLUID_RENDER_TYPE, blockPos.getX() & 0xF, blockPos.getY() & 0xF, blockPos.getZ() & 0xF);
			original.call(instance, blockPos, blockAndTintGetter, vertexConsumer, blockState, fluidState, i, j, k);
			lastBufferBuilder.endBlock();

		} else {
			original.call(instance, blockPos, blockAndTintGetter, vertexConsumer, blockState, fluidState, i, j, k);
		}

	}


	@WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V"))
	private void iris$onRenderLiquid(BlockRenderDispatcher instance, BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, Operation<Void> original) {
		if (vertexConsumer instanceof BlockSensitiveBufferBuilder lastBufferBuilder) {
			// All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
			lastBufferBuilder.beginBlock(resolveBlockId(blockState), ExtendedDataHelper.FLUID_RENDER_TYPE, blockPos.getX() & 0xF, blockPos.getY() & 0xF, blockPos.getZ() & 0xF);
			original.call(instance, blockState, blockPos, blockAndTintGetter, poseStack, vertexConsumer, bl, randomSource);
			lastBufferBuilder.endBlock();

		} else {
			original.call(instance, blockState, blockPos, blockAndTintGetter, poseStack, vertexConsumer, bl, randomSource);
		}

	}
}
