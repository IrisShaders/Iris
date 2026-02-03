package net.irisshaders.iris.mixin.fantastic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.fantastic.ParticleRenderingPhase;
import net.irisshaders.iris.fantastic.PhasedParticleEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@Mixin(ParticleFeatureRenderer.class)
public abstract class MixinParticleFeatureRenderer implements PhasedParticleEngine {
	@Shadow
	protected abstract void prepareRenderPass(RenderPass renderPass);

	@Unique
	private ParticleRenderingPhase phase = ParticleRenderingPhase.EVERYTHING;

	@Override
	public void setParticleRenderingPhase(ParticleRenderingPhase phase) {
		this.phase = phase;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getParticlesTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
	private RenderTarget iris$preventFabulousCrash(LevelRenderer instance, Operation<RenderTarget> original) {
		return phase == ParticleRenderingPhase.OPAQUE ? null : original.call(instance);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector$ParticleGroupRenderer;prepare(Lnet/minecraft/client/renderer/feature/ParticleFeatureRenderer$ParticleBufferCache;)Lnet/minecraft/client/renderer/state/QuadParticleRenderState$PreparedBuffers;"))
	private QuadParticleRenderState.PreparedBuffers iris$overrideCode(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer, ParticleFeatureRenderer.ParticleBufferCache particleBufferCache, Operation<QuadParticleRenderState.PreparedBuffers> original) {
		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.getTextureManager();
		RenderTarget renderTarget = minecraft.getMainRenderTarget();
		RenderTarget renderTarget2 = phase == ParticleRenderingPhase.OPAQUE ? null : minecraft.levelRenderer.getParticlesTarget();
		GpuDevice gpuDevice = RenderSystem.getDevice();

		QuadParticleRenderState.PreparedBuffers preparedBuffers = original.call(particleGroupRenderer, particleBufferCache);

		if (preparedBuffers != null) {
			try (RenderPass renderPass = gpuDevice.createCommandEncoder().createRenderPass(() -> "Particles - Main", renderTarget.getColorTextureView(), OptionalInt.empty(), renderTarget.getDepthTextureView(), OptionalDouble.empty())) {
				this.prepareRenderPass(renderPass);
				if (phase == ParticleRenderingPhase.EVERYTHING || phase == ParticleRenderingPhase.OPAQUE) {
					particleGroupRenderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager, false);
				}
				if (renderTarget2 == null && (phase == ParticleRenderingPhase.EVERYTHING || phase == ParticleRenderingPhase.TRANSLUCENT)) {
					particleGroupRenderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager, true);
				}
			}

			if (renderTarget2 != null && (phase == ParticleRenderingPhase.EVERYTHING || phase == ParticleRenderingPhase.TRANSLUCENT)) {
				try (RenderPass renderPass = gpuDevice.createCommandEncoder().createRenderPass(() -> "Particles - Transparent", renderTarget2.getColorTextureView(), OptionalInt.empty(), renderTarget2.getDepthTextureView(), OptionalDouble.empty())) {
					this.prepareRenderPass(renderPass);
					particleGroupRenderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager, true);
				}
			}
		}

		return null;
	}
}
