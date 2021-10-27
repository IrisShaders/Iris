package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
@Mixin(ParticleEngine.class)
@Environment(EnvType.CLIENT)
public class MixinParticleEngine {
	private static final String RENDER =
			"Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
											 LightTexture lightTexture, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = RENDER, at = @At("RETURN"))
	private void iris$finishDrawingParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
										LightTexture lightTexture, Camera camera, float f,
										CallbackInfo ci) {
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
	}
}
