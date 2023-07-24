package net.coderbot.iris.compat.sodium.mixin.separate_ao;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Basically the same as {@link MixinBlockRenderer}, but for fluid rendering.
 */
@Mixin(FluidRenderer.class)
public abstract class MixinFluidRenderer {
	@Shadow
	protected abstract void writeQuad(ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

	@Unique
    private boolean useSeparateAo;
	@Inject(method = "render", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Z)V", ordinal = 1))
	private void iris$flipNextQuad(WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkBuildBuffers buffers, CallbackInfo ci, Material material) {
		((ContextAwareVertexWriter) buffers.get(material).getVertexBuffer(ModelQuadFacing.DOWN)).flipUpcomingQuadNormal();
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Z)V", ordinal = 4))
	private void iris$flipNextQuad2(FluidRenderer renderer, ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip, WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos s, ChunkBuildBuffers buffers) {
		((ContextAwareVertexWriter) buffers.get(DefaultMaterials.forFluidState(fluidState)).getVertexBuffer(facing)).flipUpcomingQuadNormal();

		this.writeQuad(builder, material, offset, quad, facing, flip);
	}
}
