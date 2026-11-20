package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherRenderer {
	@Unique
	private static final NamespacedId WEATHER_RAIN = new NamespacedId("minecraft", "weather_rain");
	@Unique
	private static final NamespacedId WEATHER_SNOW = new NamespacedId("minecraft", "weather_snow");

	@WrapMethod(method = "render(Lnet/minecraft/client/renderer/state/level/WeatherRenderState;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)V")
	private void iris$disableWeather(WeatherRenderState renderState, RenderPass renderPass, RenderPipeline renderPipeline,
		Operation<Void> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderWeather).orElse(true)) {
			original.call(renderState, renderPass, renderPipeline);
		}
	}

	@ModifyExpressionValue(method = "prepare", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/DefaultVertexFormat;PARTICLE:Lcom/mojang/renderpearl/api/vertex/VertexFormat;"))
	private VertexFormat iris$extendWeatherFormat(VertexFormat format) {
		if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ImmediateState.skipExtension.get()) {
			return IrisVertexFormats.PARTICLE;
		}

		return format;
	}

	@WrapOperation(method = "prepare", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;prepareInstances(Lcom/mojang/blaze3d/vertex/VertexConsumer;Ljava/util/List;Lnet/minecraft/world/phys/Vec3;FIF)V"))
	private void iris$setWeatherId(WeatherEffectRenderer renderer, VertexConsumer consumer, List<?> columns, Vec3 cameraPos, float f, int i,
		float g, Operation<Void> original, @Local(argsOnly = true) WeatherRenderState renderState) {
		Object2IntFunction<NamespacedId> particleIds = WorldRenderingSettings.INSTANCE.getParticleIds();
		NamespacedId weather = columns == renderState.rainColumns ? WEATHER_RAIN : WEATHER_SNOW;

		CapturedRenderingState.INSTANCE.setCurrentRenderedParticle(
			particleIds == null ? 0 : ExtendedDataHelper.packParticleIds(particleIds.applyAsInt(weather), -1));
		original.call(renderer, consumer, columns, cameraPos, f, i, g);
		CapturedRenderingState.INSTANCE.setCurrentRenderedParticle(0);
	}

	// TODO 26.2
	//@WrapMethod(method = "extractRenderState")
	private void disableRainParticles(ClientLevel clientLevel, Camera camera, int i, ParticleStatus particleStatus, int j,
		Operation<Void> original) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderWeatherParticles).orElse(true)) {
			original.call(clientLevel, camera, i, ParticleStatus.MINIMAL, j);
		} else {
			original.call(clientLevel, camera, i, particleStatus, j);
		}
	}
}
