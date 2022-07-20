package net.coderbot.iris.compat.sodium.mixin.block_id;

import net.caffeinemc.sodium.render.chunk.compile.tasks.TerrainBuildBuffers;
import net.caffeinemc.sodium.render.chunk.compile.tasks.TerrainBuildResult;
import net.caffeinemc.sodium.render.chunk.compile.tasks.TerrainBuildTask;
import net.caffeinemc.sodium.render.chunk.state.ChunkRenderBounds;
import net.caffeinemc.sodium.render.chunk.state.ChunkRenderData;
import net.caffeinemc.sodium.render.terrain.TerrainBuildContext;
import net.caffeinemc.sodium.render.terrain.context.PreparedTerrainRenderCache;
import net.caffeinemc.sodium.util.tasks.CancellationSource;
import net.caffeinemc.sodium.world.slice.WorldSlice;
import net.coderbot.iris.compat.sodium.impl.block_context.TerrainBuildBuffersExt;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Passes material ID information indirectly to the vertex writer to support the mc_Entity part of the vertex format.
 */
@Mixin(TerrainBuildTask.class)
public class MixinTerrainBuildTask {
	@Inject(method = "performBuild", at = @At(value = "INVOKE",
		target = "net/minecraft/world/level/block/state/BlockState.getRenderShape()" +
			"Lnet/minecraft/world/level/block/RenderShape;"),
		locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$setLocalPos(TerrainBuildContext context,
								  CancellationSource cancellationSource, CallbackInfoReturnable<TerrainBuildResult> cir,
								  ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds, TerrainBuildBuffers buffers,
								  PreparedTerrainRenderCache cacheLocal,
								  WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
								  BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset,
								  int relY, int relZ, int relX) {
		if (context.buffers instanceof TerrainBuildBuffersExt) {
			((TerrainBuildBuffersExt) context.buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

    @Redirect(method = "performBuild", at = @At(value = "INVOKE",
            target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getChunkRenderType (" +
                        "Lnet/minecraft/world/level/block/state/BlockState;" +
                    ")Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType iris$wrapGetBlockLayer(BlockState blockState, TerrainBuildContext buildContext, CancellationSource cancellationSource) {
        if (buildContext.buffers instanceof TerrainBuildBuffersExt) {
            ((TerrainBuildBuffersExt) buildContext.buffers).iris$setMaterialId(blockState, (short) -1);
        }

        return ItemBlockRenderTypes.getChunkRenderType(blockState);
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE",
            target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getRenderLayer (" +
                        "Lnet/minecraft/world/level/material/FluidState;" +
                    ")Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType iris$wrapGetFluidLayer(FluidState fluidState, TerrainBuildContext buildContext, CancellationSource cancellationSource) {
        if (buildContext.buffers instanceof TerrainBuildBuffersExt) {
            // All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
            ((TerrainBuildBuffersExt) buildContext.buffers).iris$setMaterialId(fluidState.createLegacyBlock(), (short) 1);
        }

        return ItemBlockRenderTypes.getRenderLayer(fluidState);
    }

    @Inject(method = "performBuild",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockState.hasBlockEntity ()Z"))
    private void iris$resetId(TerrainBuildContext buildContext, CancellationSource source, CallbackInfoReturnable<TerrainBuildResult> cir) {
        if (buildContext.buffers instanceof TerrainBuildBuffersExt) {
            ((TerrainBuildBuffersExt) buildContext.buffers).iris$resetBlockContext();
        }
    }
}
