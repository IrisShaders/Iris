package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.client.Options;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
	private static final String RENDER = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private WorldRenderingPipeline pipeline;

	// Begin shader rendering after buffers have been cleared.
	// At this point we've ensured that Minecraft's main framebuffer is cleared.
	// This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
	// all pixels.
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER, remap = false))
	private void iris$beginLevelRender(PoseStack poseStack, float tickDelta, long startTime, boolean renderBlockOutline,
									   Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
									   Matrix4f projection, CallbackInfo callback) {
		if (Iris.isSodiumInvalid()) {
			throw new IllegalStateException("An invalid version of Sodium is installed, and the warning screen somehow" +
				" didn't work. This is a bug! Please report it to the Iris developers.");
		}

		CapturedRenderingState.INSTANCE.setGbufferModelView(poseStack.last().pose());
		CapturedRenderingState.INSTANCE.setGbufferProjection(projection);
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		SystemTimeUniforms.COUNTER.beginFrame();
		SystemTimeUniforms.TIMER.beginFrame(startTime);

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		pipeline.beginLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = RENDER, at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void iris$endLevelRender(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo callback) {
		HandRenderer.INSTANCE.renderTranslucent(poseStack, tickDelta, camera, gameRenderer, pipeline);
		Minecraft.getInstance().getProfiler().popPush("iris_final");
		pipeline.finalizeLevelRendering();
		pipeline = null;
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"))
	private void iris$renderTerrainShadows(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo callback) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY))
	private void iris$beginSky(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		// Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
		pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);
		// TODO: Move the injection instead
		RenderSystem.setShader(GameRenderer::getPositionShader);
	}

	@Inject(method = RENDER_SKY,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"))
	private void iris$renderSky$beginNormalSky(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;SUN_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
	private void iris$setSunRenderStage(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"))
	private void iris$setSunsetRenderStage(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;MOON_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
	private void iris$setMoonRenderStage(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
	private void iris$setStarRenderStage(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"))
	private void iris$setVoidRenderStage(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getTimeOfDay(F)F"),
			slice = @Slice(from = @At(value = "FIELD", target = "com/mojang/math/Vector3f.YP : Lcom/mojang/math/Vector3f;")))
	private void iris$renderSky$tiltSun(PoseStack poseStack, Matrix4f projectionMatrix, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(pipeline.getSunPathRotation()));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.CLOUDS);
	}

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void iris$maybeRemoveClouds(PoseStack poseStack, Matrix4f projectionMatrix, float f, double d, double e, double g, CallbackInfo ci) {
		if (!pipeline.shouldRenderClouds()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}


	@Inject(method = "renderChunkLayer", at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f projectionMatrix, CallbackInfo ci) {
		pipeline.setPhase(GbufferPrograms.refineTerrainPhase(renderType));
	}

	@Inject(method = "renderChunkLayer", at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f projectionMatrix, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
	}

	@ModifyArg(method = RENDER_WEATHER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", ordinal = 0, remap = false))
	private boolean iris$writeRainAndSnowToDepthBuffer(boolean depthMaskEnabled) {
		if (pipeline.shouldWriteRainAndSnowToDepthBuffer()) {
			return true;
		}

		return depthMaskEnabled;
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBorder(Lnet/minecraft/client/Camera;)V"))
	private void iris$beginWorldBorder(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBorder(Lnet/minecraft/client/Camera;)V", shift = At.Shift.AFTER))
	private void iris$endWorldBorder(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"))
	private void iris$setDebugRenderStage(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.DEBUG);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V", shift = At.Shift.AFTER))
	private void iris$resetDebugRenderStage(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"))
	private void iris$beginTranslucents(PoseStack poseStack, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightTexture lightTexture, Matrix4f projection,
										CallbackInfo ci) {
		pipeline.beginHand();
		HandRenderer.INSTANCE.renderSolid(poseStack, tickDelta, camera, gameRenderer, pipeline);
		Minecraft.getInstance().getProfiler().popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}
}
