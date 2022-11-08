package net.coderbot.iris.mixin.vertices.block_rendering;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Allows the vanilla directional shading effect to be fully disabled by shader packs. This is needed by many packs
 * because they implement their own lighting effects, which visually clash with vanilla's directional shading lighting.
 */
@Mixin(ClientLevel.class)
public class MixinClientLevel {
	@ModifyVariable(method = "getShade", at = @At("HEAD"), argsOnly = true)
	private boolean iris$maybeDisableDirectionalShading(boolean shaded) {
		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			return false;
		} else {
			return shaded;
		}
	}
}
