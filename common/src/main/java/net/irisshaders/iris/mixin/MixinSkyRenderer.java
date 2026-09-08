package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.layer.SetStateShard;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
	@Inject(method = "renderSkyDisc",
		at = @At(value = "HEAD"))
	private void iris$renderSky$beginNormalSky(RenderPass renderPass,
	                                           Vector3fc skyColor,
	                                           CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawSun(RenderPass renderPass,
	                                float rainBrightness,
	                                PoseStack poseStack,
	                                CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void iris$beforeDrawMoon(RenderPass renderPass,
	                                 MoonPhase moonPhase,
	                                 float rainBrightness,
	                                 PoseStack poseStack,
	                                 CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSun", at = @At(value = "HEAD"))
	private void iris$setSunRenderStage(RenderPass renderPass,
	                                    float rainBrightness,
	                                    PoseStack poseStack,
	                                    CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderMoon", at = @At(value = "HEAD"))
	private void iris$setMoonRenderStage(RenderPass renderPass,
	                                     MoonPhase moonPhase,
	                                     float rainBrightness,
	                                     PoseStack poseStack,
	                                     CallbackInfo ci) {
		setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderSunriseAndSunset", at = @At(value = "HEAD"))
	private void iris$setSunsetRenderStage(RenderPass renderPass,
	                                       PoseStack poseStack,
	                                       float sunAngle,
	                                       Vector4fc sunriseAndSunsetColor,
	                                       CallbackInfo ci) {
		setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderStars", at = @At(value = "HEAD"))
	private void iris$setStarRenderStage(RenderPass renderPass,
	                                     float starBrightness,
	                                     PoseStack poseStack,
	                                     CallbackInfo ci) {
		setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderDarkDisc", at = @At(value = "HEAD"))
	private void iris$setVoidRenderStage(CallbackInfo ci) {
		setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 0, shift = At.Shift.AFTER))
	private void iris$renderSky$tiltSun(RenderPass renderPass,
	                                    PoseStack poseStack,
	                                    float sunAngle,
	                                    float moonAngle,
	                                    float starAngle,
	                                    MoonPhase moonPhase,
	                                    float rainBrightness,
	                                    float starBrightness,
	                                    CallbackInfo ci) {
		poseStack.rotateDegrees(Axis.ZP, getSunPathRotation());
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
