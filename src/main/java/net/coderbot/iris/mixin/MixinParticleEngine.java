package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.coderbot.iris.block_rendering.ParticleIdMapper;
import net.coderbot.iris.fantastic.ParticleIdHolder;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine {
	private static final String RENDER =
			"Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
											 LightTexture lightTexture, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.setPhase(WorldRenderingPhase.PARTICLES);
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void captureParticles(PoseStack arg, MultiBufferSource.BufferSource arg2, LightTexture arg3, Camera arg4, float f, CallbackInfo ci, Iterator var6, ParticleRenderType lv, Iterable iterable, Tesselator lv2, BufferBuilder lv3, Iterator var11, Particle lv4) {
		ParticleIdMapper.instance.setCurrentParticle(lv4);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void resetParticles(PoseStack arg, MultiBufferSource.BufferSource arg2, LightTexture arg3, Camera arg4, float f, CallbackInfo ci, Iterator var6, ParticleRenderType lv, Iterable iterable, Tesselator lv2, BufferBuilder lv3, Iterator var11, Particle lv4) {
		ParticleIdMapper.instance.resetParticle();
	}

	@Inject(method = RENDER, at = @At("RETURN"))
	private void iris$finishDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
										LightTexture lightTexture, Camera camera, float f,
										CallbackInfo ci) {
		GbufferPrograms.setPhase(WorldRenderingPhase.NONE);
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "makeParticle", at = @At("RETURN"))
	private void setParticleType(ParticleOptions arg, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<@Nullable Particle> cir) {
		((ParticleIdHolder) cir.getReturnValue()).setParticleId(arg.getType());
	}
}
