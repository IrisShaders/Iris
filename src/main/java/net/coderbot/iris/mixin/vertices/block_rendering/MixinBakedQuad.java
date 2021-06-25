package net.coderbot.iris.mixin.vertices.block_rendering;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows the vanilla directional shading effect to be fully disabled by shader packs. This is needed by many packs
 * because they implement their own lighting effects, which visually clash with vanilla's directional shading lighting.
 */
@Mixin(BakedQuad.class)
public class MixinBakedQuad {
	@Inject(method = "hasShade()Z", at = @At("HEAD"), cancellable = true)
	private void hasShade(CallbackInfoReturnable<Boolean> cir) {
		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			cir.setReturnValue(false);
		}
	}
}
