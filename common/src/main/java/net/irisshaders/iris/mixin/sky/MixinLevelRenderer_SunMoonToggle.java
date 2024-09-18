package net.irisshaders.iris.mixin.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.LevelHeightAccessor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Allows pipelines to disable the sun, moon, or both.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_SunMoonToggle {
	@WrapOperation(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.SUN_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;")),
		allow = 1)
	private void iris$beforeDrawSun(MeshData meshData, Operation<Void> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			original.call(meshData);
		} else {
			meshData.close();
		}
	}

	@WrapOperation(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.getStarBrightness (F)F")),
		allow = 1)
	private void iris$beforeDrawMoon(MeshData meshData, Operation<Void> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			original.call(meshData);
		} else {
			meshData.close();
		}
	}


	@WrapOperation(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"),
			to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;unbind()V", ordinal = 0)),
		allow = 1)
	private void iris$beforeDrawSkyDisc(VertexBuffer instance, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, Operation<Void> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			original.call(instance, modelViewMatrix, projectionMatrix, shader);
		}
	}

	@WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"))
	private float[] iris$beforeDrawHorizon(DimensionSpecialEffects instance, float timeOfDay, float partialTicks, Operation<float[]> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			return original.call(instance, timeOfDay, partialTicks);
		} else {
			return null;
		}
	}

	@WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getHorizonHeight(Lnet/minecraft/world/level/LevelHeightAccessor;)D"))
	private double iris$beforeDrawHorizon(ClientLevel.ClientLevelData instance, LevelHeightAccessor level, Operation<Double> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			return original.call(instance, level);
		} else {
			return Double.NEGATIVE_INFINITY;
		}
	}

	@WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
	private float iris$beforeDrawStars(ClientLevel instance, float partialTick, Operation<Float> original) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderStars).orElse(true)) {
			return original.call(instance, partialTick);
		} else {
			return -0.1f;
		}
	}
}
