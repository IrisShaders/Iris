package net.coderbot.iris.mixin.sky;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Allows pipelines to disable the sun, moon, or both.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_SunMoonToggle {
	@Redirect(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.SUN_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;")),
		allow = 1)
	private void iris$beforeDrawSun(BufferBuilder.RenderedBuffer arg) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			BufferUploader.drawWithShader(arg);
		} else {
			arg.release();
		}
	}

	@Redirect(method = "renderSky",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.getStarBrightness (F)F")),
		allow = 1)
	private void iris$beforeDrawMoon(BufferBuilder.RenderedBuffer arg) {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			BufferUploader.drawWithShader(arg);
		} else {
			arg.release();
		}
	}
}
