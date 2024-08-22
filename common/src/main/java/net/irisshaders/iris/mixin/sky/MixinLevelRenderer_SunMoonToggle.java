package net.irisshaders.iris.mixin.sky;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.LevelRenderer;
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
}
