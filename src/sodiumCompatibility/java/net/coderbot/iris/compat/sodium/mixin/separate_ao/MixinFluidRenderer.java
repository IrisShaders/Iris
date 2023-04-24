package net.coderbot.iris.compat.sodium.mixin.separate_ao;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Basically the same as {@link MixinBlockRenderer}, but for fluid rendering.
 */
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds$Builder;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Z)V", ordinal = 1))
	private void iris$flipNextQuad(FluidRenderer renderer, ChunkModelBuilder builder, ChunkRenderBounds.Builder bounds, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip, BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset2, ChunkBuildBuffers buffers) {
		((ContextAwareVertexWriter) buffers.get(DefaultMaterials.forFluidState(fluidState)).getVertexBuffer(facing)).flipUpcomingQuadNormal();
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds$Builder;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Z)V", ordinal = 4))
	private void iris$flipNextQuad2(FluidRenderer renderer, ChunkModelBuilder builder, ChunkRenderBounds.Builder bounds, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip, BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset2, ChunkBuildBuffers buffers) {
		((ContextAwareVertexWriter) buffers.get(DefaultMaterials.forFluidState(fluidState)).getVertexBuffer(facing)).flipUpcomingQuadNormal();
	}

}
