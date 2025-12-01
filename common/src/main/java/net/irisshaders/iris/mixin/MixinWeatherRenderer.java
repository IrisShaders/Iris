package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherRenderer {
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;useShaderTransparency()Z"))
	private boolean iris$writeRainAndSnowToDepthBuffer() {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldWriteRainAndSnowToDepthBuffer).orElse(false)) {
			return true;
		}

		return Minecraft.useShaderTransparency();
	}

	@WrapMethod(method = "render")
	private void iris$disableWeather(MultiBufferSource multiBufferSource, Vec3 vec3, WeatherRenderState weatherRenderState, Operation<Void> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderWeather).orElse(true)) {
			original.call(multiBufferSource, vec3, weatherRenderState);
		}
	}

	@WrapMethod(method = "tickRainParticles")
	private void disableRainParticles(ClientLevel clientLevel, Camera camera, int i, ParticleStatus particleStatus, Operation<Void> original) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderWeatherParticles).orElse(true)) {
			original.call(clientLevel, camera, i, ParticleStatus.MINIMAL);
		} else {
			original.call(clientLevel, camera, i, particleStatus);
		}
	}
}
