package net.irisshaders.iris.mixin.fantastic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.fantastic.ParticleRenderingPhase;
import net.irisshaders.iris.fantastic.PhasedParticleEngine;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.properties.ParticleRenderingSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * Uses the PhasedParticleManager changes to render opaque particles much earlier than other particles.
 * <p>
 * See the comments in {@link MixinParticleEngine} for more details.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private RenderBuffers renderBuffers;

	@Shadow
	public abstract Frustum getFrustum();

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V", ordinal = 1))
	private void redirectSolidParticles(ParticleEngine instance, LightTexture lightTexture, Camera camera, float v, Frustum frustum, Predicate<ParticleRenderType> predicate, Operation<Void> original) {
		ParticleRenderingSettings settings = getRenderingSettings();

		Predicate<ParticleRenderType> newPredicate = predicate;

		if (settings == ParticleRenderingSettings.BEFORE) {
			newPredicate = (t) -> true;
		} else if (settings == ParticleRenderingSettings.AFTER) {
			return;
		}

		original.call(instance, lightTexture, camera, v, frustum, newPredicate);
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V", ordinal = 2))
	private void redirectTransParticles(ParticleEngine instance, LightTexture lightTexture, Camera camera, float v, Frustum frustum, Predicate<ParticleRenderType> predicate, Operation<Void> original) {
		ParticleRenderingSettings settings = getRenderingSettings();

		Predicate<ParticleRenderType> newPredicate = predicate;

		if (settings == ParticleRenderingSettings.BEFORE) {
			return;
		} else if (settings == ParticleRenderingSettings.AFTER) {
			newPredicate = (t) -> true;
		}

		original.call(instance, lightTexture, camera, v, frustum, newPredicate);
	}

	private ParticleRenderingSettings getRenderingSettings() {
		return Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::getParticleRenderingSettings).orElse(ParticleRenderingSettings.MIXED);
	}
}
