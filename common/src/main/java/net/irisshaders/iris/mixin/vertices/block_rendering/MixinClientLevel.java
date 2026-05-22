package net.irisshaders.iris.mixin.vertices.block_rendering;

import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Allows the vanilla directional shading effect to be fully disabled by shader packs. This is needed by many packs
 * because they implement their own lighting effects, which visually clash with vanilla's directional shading lighting.
 */
@Mixin(CardinalLighting.class)
public class MixinClientLevel {
	@ModifyVariable(method = "byFace", at = @At("HEAD"), argsOnly = true)
	private Direction iris$maybeDisableDirectionalShading(Direction value) {
		if (WorldRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			return Direction.UP;
		} else {
			return value;
		}
	}
}
