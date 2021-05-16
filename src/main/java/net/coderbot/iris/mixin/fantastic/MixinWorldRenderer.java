package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.ParticleRenderingPhase;
import net.coderbot.iris.fantastic.PhasedParticleManager;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uses the PhasedParticleManager changes to render opaque particles much earlier than other particles.
 *
 * See the comments in {@link MixinParticleManager} for more details.
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;

	@Inject(method = "render", at = @At("HEAD"))
	private void iris$resetParticleManagerPhase(MatrixStack matrices, float tickDelta, long limitTime,
												boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
												LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
												CallbackInfo callback) {
		((PhasedParticleManager) client.particleManager).setParticleRenderingPhase(ParticleRenderingPhase.EVERYTHING);
	}

	@Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=entities"))
	private void iris$renderOpaqueParticles(MatrixStack matrices, float tickDelta, long limitTime,
												boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
												LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
												CallbackInfo callback) {
		VertexConsumerProvider.Immediate immediate = bufferBuilders.getEntityVertexConsumers();

		((PhasedParticleManager) client.particleManager).setParticleRenderingPhase(ParticleRenderingPhase.OPAQUE);

		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
		client.particleManager.renderParticles(matrices, immediate, lightmapTextureManager, camera, tickDelta);
		GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);

		((PhasedParticleManager) client.particleManager).setParticleRenderingPhase(ParticleRenderingPhase.TRANSLUCENT);
	}
}
