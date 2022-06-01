package net.coderbot.iris.compat.sodium.mixin.directional_shading;

import me.jellysquid.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlatLightPipeline.class)
public class MixinFlatLightPipeline {
	@Redirect(method = "calculate", at = @At(value = "INVOKE",
			target = "net/minecraft/world/level/BlockAndTintGetter.getShade (Lnet/minecraft/core/Direction;Z)F"))
	private float iris$getBrightness(BlockAndTintGetter level, Direction direction, boolean shaded) {
		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			return 1.0F;
		} else {
			return level.getShade(direction, shaded);
		}
	}
}
