package net.coderbot.iris.compat.sodium.mixin.better_mipmaps;

import me.jellysquid.mods.sodium.render.chunk.passes.DefaultRenderPasses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Uses an appropriate alpha test value with CUTOUT_MIPPED content, needed for the Iris
 * better_mipmaps vanilla patches to work.
 */
@Mixin(DefaultRenderPasses.class)
public class MixinDefaultRenderPasses {
	@ModifyArg(method = "<clinit>",
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/render/chunk/passes/ChunkRenderPass.<init> (" +
							"Lme/jellysquid/mods/sodium/opengl/types/RenderState;ZF" +
							")V"))
	private float iris$tweakCutoutMippedAlphaThreshold(float threshold) {
		if (threshold == 0.5f) {
			return 0.1f;
		} else {
			return threshold;
		}
	}
}
