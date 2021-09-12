package net.coderbot.iris.mixin.fantastic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.fantastic.ParticleRenderingPhase;
import net.coderbot.iris.fantastic.PhasedParticleEngine;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uses the PhasedParticleManager changes to render opaque particles much earlier than other particles.
 *
 * See the comments in {@link MixinParticleEngine} for more details.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$resetParticleManagerPhase(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		((PhasedParticleEngine) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.EVERYTHING);
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=entities"))
	private void iris$renderOpaqueParticles(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		minecraft.getProfiler().popPush("opaque_particles");

		MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();

		((PhasedParticleEngine) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.OPAQUE);

		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
		minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);

		((PhasedParticleEngine) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.TRANSLUCENT);
	}
}
