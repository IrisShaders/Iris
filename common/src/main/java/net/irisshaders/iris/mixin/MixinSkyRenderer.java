package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
	@Inject(method = "renderSkyDisc",
		at = @At(value = "HEAD"))
	private void iris$renderSky$beginNormalSky(float f, float g, float h, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawSun(float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawMoon(int i, float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSun", at = @At(value = "HEAD"))
	private void iris$setSunRenderStage(float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSunriseAndSunset", at = @At(value = "HEAD"))
	private void iris$setSunsetRenderStage(PoseStack poseStack, Tesselator tesselator, float f, int i, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderMoon", at = @At(value = "HEAD"))
	private void iris$setMoonRenderStage(int i, float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderStars", at = @At(value = "HEAD"))
	private void iris$setStarRenderStage(FogParameters fogParameters, float f, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderDarkDisc", at = @At(value = "HEAD"))
	private void iris$setVoidRenderStage(PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 1))
	private void iris$renderSky$tiltSun(PoseStack poseStack, Tesselator tesselator, float f, int i, float g, float h, FogParameters fogParameters, CallbackInfo ci) {
		poseStack.mulPose(Axis.ZP.rotationDegrees(getSunPathRotation()));
	}

	private float getSunPathRotation() {
		if (Iris.getPipelineManager().getPipelineNullable() == null) return 0;
		return Iris.getPipelineManager().getPipelineNullable().getSunPathRotation();
	}

	public void setPhase(WorldRenderingPhase phase) {
		if (Iris.getPipelineManager().getPipelineNullable() == null) return;

		Iris.getPipelineManager().getPipelineNullable().setPhase(phase);
	}
}
