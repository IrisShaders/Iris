package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Iterator;
import java.util.Objects;

import net.irisshaders.iris.layer.GbufferProgram;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.pipeline.DeferredWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(ParticleEngine.class)
@Environment(EnvType.CLIENT)
public class MixinParticleEngine {
	private static final String RENDER_PARTICLES = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V";
	private static final String DRAW = "Lnet/minecraft/client/particle/ParticleRenderType;end(Lcom/mojang/blaze3d/vertex/Tesselator;)V";

	@Unique
	private ParticleRenderType lastSheet;

	@Inject(method = RENDER_PARTICLES, at = @At("HEAD"))
	private void iris$beginDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
											 LightTexture lightTexture, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = RENDER_PARTICLES, at = @At(value = "INVOKE", target = DRAW), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$preDrawParticleSheet(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
										LightTexture lightTexture, Camera camera, float f,
										CallbackInfo ci, Iterator<ParticleRenderType> sheets, ParticleRenderType sheet,
										Iterable<Particle> particles, Tesselator tessellator) {
		GbufferPrograms.push(DeferredWorldRenderingPipeline.getProgramForSheet(sheet));

		if (lastSheet != null) {
			throw new IllegalStateException("Particle rendering in weird state: lastSheet != null, lastSheet = " + lastSheet);
		}

		lastSheet = sheet;
	}

	@Inject(method = RENDER_PARTICLES, at = @At(value = "INVOKE", target = DRAW, shift = At.Shift.AFTER))
	private void iris$postDrawParticleSheet(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
											 LightTexture lightTexture, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.pop(DeferredWorldRenderingPipeline.getProgramForSheet(Objects.requireNonNull(lastSheet)));
		lastSheet = null;
	}

	@Inject(method = RENDER_PARTICLES, at = @At("RETURN"))
	private void iris$finishDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
										LightTexture lightTexture, Camera camera, float f,
										CallbackInfo ci) {
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
	}
}
