package net.coderbot.iris.compat.sodium.mixin.separate_ao;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows vertex AO to be optionally passed in the alpha channel of the vertex color instead of being multiplied
 * through into the RGB values.
 */
@Mixin(BlockRenderer.class)
public class MixinBlockRenderer {
    @Unique
    private boolean useSeparateAo;

    @Inject(method = "renderModel", remap = false, at = @At("HEAD"))
    private void renderModel(BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin,
							 BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed,
							 CallbackInfoReturnable<Boolean> cir) {
        this.useSeparateAo = BlockRenderingSettings.INSTANCE.shouldUseSeparateAo();
    }

    @Redirect(method = "renderQuad", remap = false,
            at = @At(value = "INVOKE",
                    target = "me/jellysquid/mods/sodium/client/util/color/ColorABGR.mul (IF)I",
                    remap = false))
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
