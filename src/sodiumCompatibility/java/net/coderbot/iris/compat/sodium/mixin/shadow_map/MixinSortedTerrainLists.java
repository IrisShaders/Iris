package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.draw.SortedTerrainLists;
import net.caffeinemc.sodium.render.chunk.state.ChunkRenderBounds;
import net.caffeinemc.sodium.render.terrain.quad.properties.ChunkMeshFace;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SortedTerrainLists.class)
public class MixinSortedTerrainLists {
	@Inject(method = "calculateCameraVisibilityBits", at = @At("HEAD"), cancellable = true, remap = false)
	private static void iris$modifyVisibilityBitsShadowPass(ChunkRenderBounds bounds, ChunkCameraContext camera, CallbackInfoReturnable<Integer> cir) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			cir.setReturnValue(ChunkMeshFace.ALL_BITS);
		}
	}
}
