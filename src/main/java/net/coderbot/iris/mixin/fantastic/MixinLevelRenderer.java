package net.coderbot.iris.mixin.fantastic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.coderbot.iris.fantastic.ParticleRenderingPhase;
import net.coderbot.iris.fantastic.PhasedParticleManager;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
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
	private void iris$resetParticleManagerPhase(PoseStack matrices, float tickDelta, long limitTime,
												boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
												LightTexture lightmapTextureManager, Matrix4f matrix4f,
												CallbackInfo callback) {
		((PhasedParticleManager) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.EVERYTHING);
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=entities"))
	private void iris$renderOpaqueParticles(PoseStack matrices, float tickDelta, long limitTime,
												boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
												LightTexture lightmapTextureManager, Matrix4f matrix4f,
												CallbackInfo callback) {
		minecraft.getProfiler().popPush("opaque_particles");

		MultiBufferSource.BufferSource immediate = renderBuffers.bufferSource();

		((PhasedParticleManager) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.OPAQUE);

		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
		minecraft.particleEngine.render(matrices, immediate, lightmapTextureManager, camera, tickDelta);
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);

		((PhasedParticleManager) minecraft.particleEngine).setParticleRenderingPhase(ParticleRenderingPhase.TRANSLUCENT);
	}

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$fantastic$beginWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		((ExtendedBufferStorage) renderBuffers).beginWorldRendering();
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void iris$fantastic$endWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		((ExtendedBufferStorage) renderBuffers).endWorldRendering();
	}
}
