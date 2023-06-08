package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.coderbot.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.batchedentityrendering.impl.RenderBuffersExt;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Tracks whether or not the world is being rendered, and manages grouping
 * with different entities.
 */
// Uses a priority of 999 to apply before the main Iris mixins to draw entities before deferred runs.
@Mixin(value = LevelRenderer.class, priority = 999)
public class MixinLevelRenderer {
	private static final String RENDER_ENTITY =
			"Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V";

	@Shadow
	private RenderBuffers renderBuffers;

	@Unique
	private Groupable groupable;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void batchedentityrendering$beginLevelRender(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (renderBuffers instanceof DrawCallTrackingRenderBuffers) {
			((DrawCallTrackingRenderBuffers) renderBuffers).resetDrawCounts();
		}

		((RenderBuffersExt) renderBuffers).beginLevelRendering();
		MultiBufferSource provider = renderBuffers.bufferSource();

		if (provider instanceof Groupable) {
			groupable = (Groupable) provider;
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_ENTITY))
	private void batchedentityrendering$preRenderEntity(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (groupable != null) {
			groupable.startGroup();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_ENTITY, shift = At.Shift.AFTER))
	private void batchedentityrendering$postRenderEntity(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (groupable != null) {
			groupable.endGroup();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void batchedentityrendering$beginTranslucents(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (renderBuffers.bufferSource() instanceof FullyBufferedMultiBufferSource fullyBufferedMultiBufferSource) {
			fullyBufferedMultiBufferSource.readyUp();
		}

		if (BlockRenderingSettings.INSTANCE.shouldSeparateEntityDraws()) {
			Minecraft.getInstance().getProfiler().popPush("entity_draws_opaque");
			if (renderBuffers.bufferSource() instanceof FullyBufferedMultiBufferSource source) {
				source.endBatchWithType(TransparencyType.OPAQUE);
			} else {
				this.renderBuffers.bufferSource().endBatch();
			}
		} else {
			Minecraft.getInstance().getProfiler().popPush("entity_draws");
			this.renderBuffers.bufferSource().endBatch();
		}
	}


	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void batchedentityrendering$endTranslucents(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.shouldSeparateEntityDraws()) {
			this.renderBuffers.bufferSource().endBatch();
		}
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void batchedentityrendering$endLevelRender(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		((RenderBuffersExt) renderBuffers).endLevelRendering();
		groupable = null;
	}
}
