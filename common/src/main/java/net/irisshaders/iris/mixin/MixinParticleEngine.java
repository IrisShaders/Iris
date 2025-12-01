package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
@Mixin(ParticleFeatureRenderer.class)
public class MixinParticleEngine {
	@Unique
	private WorldRenderingPhase lastPhase = WorldRenderingPhase.NONE;

	@Inject(method = "render", at = @At("HEAD"))
	private void iris$beginDrawingParticles(SubmitNodeCollection submitNodeCollection, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> {
			lastPhase = pipeline.getPhase();
			pipeline.setPhase(WorldRenderingPhase.PARTICLES);
		});
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$finishDrawingParticles(SubmitNodeCollection submitNodeCollection, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(lastPhase));
	}
}
