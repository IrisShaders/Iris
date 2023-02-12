package net.coderbot.iris.compat.sodium.mixin.separate_ao;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Basically the same as {@link MixinBlockRenderer}, but for fluid rendering.
 */
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
    @Unique
    private boolean useSeparateAo;

    @Inject(method = "render", remap = false, at = @At("HEAD"))
    private void iris$cacheSeparateAoSetting(BlockAndTintGetter world, FluidState fluidState, BlockPos pos,
											 BlockPos offset, ChunkModelBuilder buffers,
											 CallbackInfoReturnable<Boolean> cir) {
        this.useSeparateAo = BlockRenderingSettings.INSTANCE.shouldUseSeparateAo();
    }

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadWinding;)V", ordinal = 1))
	private void iris$flipNextQuad(BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> cir) {
		((ContextAwareVertexWriter) buffers.getVertexBuffer()).flipUpcomingQuadNormal();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;writeQuad(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadFacing;Lme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadWinding;)V", ordinal = 4))
	private void iris$flipNextQuad2(BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> cir) {
		((ContextAwareVertexWriter) buffers.getVertexBuffer()).flipUpcomingQuadNormal();
	}

    @Redirect(method = "updateQuad", remap = false,
            at = @At(value = "INVOKE", target = "me/jellysquid/mods/sodium/client/util/color/ColorABGR.mul (IF)I", remap = false))
    private int iris$applySeparateAo(int color, float ao) {
        if (useSeparateAo) {
            color &= 0x00FFFFFF;
            color |= ((int) (ao * 255.0f)) << 24;
        } else {
            color = ColorABGR.mul(color, ao);
        }

        return color;
    }
}
