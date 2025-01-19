package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DefaultFluidRenderer.class)
public class MixinDefaultFluidRenderer {
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/" +
		"pipeline/DefaultFluidRenderer;updateQuad(Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;" +
		"Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/" +
		"mods/sodium/client/model/light/LightPipeline;Lnet/minecraft/core/Direction;Lnet/caffeinemc/mods/" +
		"sodium/client/model/quad/properties/ModelQuadFacing" +
		";FLnet/caffeinemc/mods/sodium/client/model/color/ColorProvider;Lnet/minecraft/world/level/material/FluidState;)V", ordinal = 2))
	private float setBrightness(float br) {
		return WorldRenderingSettings.INSTANCE.shouldDisableDirectionalShading() ? 1.0f : br;
	}
}
