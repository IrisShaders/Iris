package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherRenderer {
	@ModifyArg(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/world/phys/Vec3;IFLjava/util/List;Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", ordinal = 0, remap = false))
	private boolean iris$writeRainAndSnowToDepthBuffer(boolean depthMaskEnabled) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldWriteRainAndSnowToDepthBuffer).orElse(false)) {
			return true;
		}

		return depthMaskEnabled;
	}
}
