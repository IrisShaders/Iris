package net.irisshaders.iris.mixin.sky;

import com.mojang.blaze3d.vertex.BufferUploader;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Allows pipelines to disable the sun, moon, or both.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_SunMoonToggle {
	@Redirect(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.SUN_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;")),
		allow = 1)
	private void iris$beforeDrawSun(MeshData meshData) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			BufferUploader.drawWithShader(meshData);
		} else {
			meshData.close();
		}
	}

	@Redirect(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.getStarBrightness (F)F")),
		allow = 1)
	private void iris$beforeDrawMoon(MeshData meshData) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			BufferUploader.drawWithShader(meshData);
		} else {
			meshData.close();
		}
	}


	@Redirect(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"),
			to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;unbind()V", ordinal = 0)),
		allow = 1)
	private void iris$beforeDrawSkyDisc(VertexBuffer instance, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			instance.drawWithShader(modelViewMatrix, projectionMatrix, shader);
		}
	}

	@Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"))
	private float[] iris$beforeDrawHorizon(DimensionSpecialEffects instance, float timeOfDay, float partialTicks) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			return instance.getSunriseColor(timeOfDay, partialTicks);
		} else {
			return null;
		}
	}

	@Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getHorizonHeight(Lnet/minecraft/world/level/LevelHeightAccessor;)D"))
	private double iris$beforeDrawHorizon(ClientLevel.ClientLevelData instance, LevelHeightAccessor level) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSkyDisc).orElse(true)) {
			return instance.getHorizonHeight(level);
		} else {
			return Double.NEGATIVE_INFINITY;
		}
	}

	@Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
	private float iris$beforeDrawStars(ClientLevel instance, float partialTick) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderStars).orElse(true)) {
			return instance.getStarBrightness(partialTick);
		} else {
			return -0.1f;
		}
	}
}
