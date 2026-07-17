package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
@Mixin(QuadParticleFeatureRenderer.class)
public class MixinParticleEngine {
	@Unique
	private WorldRenderingPhase lastPhase = WorldRenderingPhase.NONE;

	@Inject(method = "executeGroup", at = @At("HEAD"))
	private void iris$beginDrawingParticles(FeatureFrameContext context, OitStage stage, RenderPass renderPass, int groupIndex, List<QuadParticleFeatureRenderer.Submit> submits, boolean strictlyOrdered, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> {
			lastPhase = pipeline.getPhase();
			pipeline.setPhase(WorldRenderingPhase.PARTICLES);
		});
	}

	@Inject(method = "executeGroup", at = @At("RETURN"))
	private void iris$finishDrawingParticles(FeatureFrameContext context, OitStage stage, RenderPass renderPass, int groupIndex, List<QuadParticleFeatureRenderer.Submit> submits, boolean strictlyOrdered, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(lastPhase));
	}
}
