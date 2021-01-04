package net.coderbot.iris.mixin;

import java.util.Iterator;

import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
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

	@Inject(method = RENDER_PARTICLES, at = @At(value = "INVOKE", target = DRAW), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$preDrawParticleSheet(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
										LightmapTextureManager lightmapTextureManager, Camera camera, float f,
										CallbackInfo ci, Iterator<ParticleTextureSheet> sheets, ParticleTextureSheet sheet,
										Iterable<Particle> particles, Tessellator tessellator) {
		Iris.getPipeline().beginParticleSheet(sheet);
	}

	@Inject(method = RENDER_PARTICLES, at = @At("RETURN"))
	private void iris$finishDrawingParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate,
										LightmapTextureManager lightmapTextureManager, Camera camera, float f,
										CallbackInfo ci) {
		Iris.getPipeline().endParticles();
	}
}
