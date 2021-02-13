package net.coderbot.iris.mixin;

import java.util.Iterator;
import java.util.Objects;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.ShaderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(ParticleManager.class)
@Environment(EnvType.CLIENT)
public class MixinParticleManager {
	private static final String RENDER_PARTICLES = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V";
	private static final String DRAW = "Lnet/minecraft/client/particle/ParticleTextureSheet;draw(Lnet/minecraft/client/render/Tessellator;)V";

	@Unique
	private ParticleTextureSheet lastSheet;

	@Inject(method = RENDER_PARTICLES, at = @At("HEAD"))
	private void iris$beginDrawingParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
											 LightmapTextureManager lightmapTextureManager, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = RENDER_PARTICLES, at = @At(value = "INVOKE", target = DRAW), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$preDrawParticleSheet(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
										LightmapTextureManager lightmapTextureManager, Camera camera, float f,
										CallbackInfo ci, Iterator<ParticleTextureSheet> sheets, ParticleTextureSheet sheet,
										Iterable<Particle> particles, Tessellator tessellator) {
		GbufferPrograms.push(ShaderPipeline.getProgramForSheet(sheet));

		if (lastSheet != null) {
			throw new IllegalStateException("Particle rendering in weird state: lastSheet != null, lastSheet = " + lastSheet);
		}

		lastSheet = sheet;
	}

	@Inject(method = RENDER_PARTICLES, at = @At(value = "INVOKE", target = DRAW, shift = At.Shift.AFTER))
	private void iris$postDrawParticleSheet(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
											 LightmapTextureManager lightmapTextureManager, Camera camera, float f,
											 CallbackInfo ci) {
		GbufferPrograms.pop(ShaderPipeline.getProgramForSheet(Objects.requireNonNull(lastSheet)));
		lastSheet = null;
	}

	@Inject(method = RENDER_PARTICLES, at = @At("RETURN"))
	private void iris$finishDrawingParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
										LightmapTextureManager lightmapTextureManager, Camera camera, float f,
										CallbackInfo ci) {
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
	}
}
