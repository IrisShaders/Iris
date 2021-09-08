package net.coderbot.iris.mixin.vertices.block_rendering;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Allows the vanilla directional shading effect to be fully disabled by shader packs. This is needed by many packs
 * because they implement their own lighting effects, which visually clash with vanilla's directional shading lighting.
 */
@Mixin(ClientWorld.class)
public class MixinClientWorld {
	@ModifyVariable(method = "getBrightness(Lnet/minecraft/util/math/Direction;Z)F", at = @At("HEAD"))
	private boolean iris$maybeDisableDirectionalShading(boolean shaded) {
		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			return false;
		} else {
			return shaded;
		}
	}
}
