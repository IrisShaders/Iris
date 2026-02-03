package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.layer.SetStateShard;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
	@Inject(method = "renderSkyDisc",
		at = @At(value = "HEAD"))
	private void iris$renderSky$beginNormalSky(int i, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawSun(float f, PoseStack poseStack, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawMoon(MoonPhase moonPhase, float f, PoseStack poseStack, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSun", at = @At(value = "HEAD"))
	private void iris$setSunRenderStage(float f, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderMoon", at = @At(value = "HEAD"))
	private void iris$setMoonRenderStage(MoonPhase moonPhase, float f, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderSunriseAndSunset", at = @At(value = "HEAD"))
	private void iris$setSunsetRenderStage(PoseStack poseStack, float f, int i, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderStars", at = @At(value = "HEAD"))
	private void iris$setStarRenderStage(float f, PoseStack poseStack, CallbackInfo ci) {
		setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderDarkDisc", at = @At(value = "HEAD"))
	private void iris$setVoidRenderStage(CallbackInfo ci) {
		setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 0, shift = At.Shift.AFTER))
	private void iris$renderSky$tiltSun(PoseStack poseStack, float f, float g, float h, MoonPhase moonPhase, float i, float j, CallbackInfo ci) {
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
