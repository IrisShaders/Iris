package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinWorldRenderer {
	private static final String RENDER_LEVEL = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;F)V";
	private static final String RENDER_LAYER = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDD)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";
	private static final String RENDER_WORLD_BOUNDS = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBounds(Lnet/minecraft/client/Camera;)V";
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V";

	@Unique
	private boolean skyTextureEnabled;

	@Unique
	private WorldRenderingPipeline pipeline;

	@Inject(method = RENDER_LEVEL, at = @At("HEAD"))
	private void iris$beginWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		if (Iris.isSodiumInvalid()) {
			throw new IllegalStateException("An invalid version of Sodium is installed, and the warning screen somehow" +
					" didn't work. This is a bug! Please report it to the Iris developers.");
		}

		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.last().pose());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);

		Program.unbind();

		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());

		pipeline.beginWorldRendering();
	}

	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = RENDER_LEVEL, at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void iris$endWorldRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Minecraft.getInstance().getProfiler().popPush("iris_final");
		pipeline.finalizeWorldRendering();
		pipeline = null;
		Program.unbind();
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;compileChunksUntil(J)V", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR))
	private void iris$beforeClear(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
	private void iris$afterClear(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY))
	private void iris$beginSky(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.SKY_TEXTURED);
		skyTextureEnabled = true;
	}

	@Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;renderDistance:I"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V")))
	private int iris$alwaysRenderSky(Options options) {
		return Math.max(options.renderDistance, 4);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V"))
	private void iris$renderSky$disableTexture(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		if (skyTextureEnabled) {
			skyTextureEnabled = false;
			pipeline.pushProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = "renderSky",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"))
	private void iris$renderSky$drawHorizon(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		RenderSystem.depthMask(false);

		Vec3 fogColor = CapturedRenderingState.INSTANCE.getFogColor();
		RenderSystem.color3f((float) fogColor.x, (float) fogColor.y, (float) fogColor.z);

		new HorizonRenderer().renderHorizon(matrices);

		RenderSystem.depthMask(true);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSunAngle(F)F"),
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;y:D")))
	private void iris$renderSky$tiltSun(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		matrices.mulPose(Vector3f.ZP.rotationDegrees(pipeline.getSunPathRotation()));
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V"))
	private void iris$renderSky$enableTexture(PoseStack matrices, float tickDelta, CallbackInfo callback) {
		if (!skyTextureEnabled) {
			skyTextureEnabled = true;
			pipeline.popProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		pipeline.popProgram(GbufferProgram.SKY_TEXTURED);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void iris$maybeRemoveClouds(PoseStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (!pipeline.shouldRenderClouds()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = RENDER_LAYER, at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderType == RenderType.solid() || renderType == RenderType.cutout() || renderType == RenderType.cutoutMipped()) {
			pipeline.pushProgram(GbufferProgram.TERRAIN);
		} else if (renderType == RenderType.translucent() || renderType == RenderType.tripwire()) {
			pipeline.pushProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderType);
		}
	}

	@Inject(method = RENDER_LAYER, at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderType == RenderType.solid() || renderType == RenderType.cutout() || renderType == RenderType.cutoutMipped()) {
			pipeline.popProgram(GbufferProgram.TERRAIN);
		} else if (renderType == RenderType.translucent() || renderType == RenderType.tripwire()) {
			pipeline.popProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderType);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BOUNDS))
	private void iris$beginWorldBorder(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BOUNDS, shift = At.Shift.AFTER))
	private void iris$endWorldBorder(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultAlphaFunc()V", shift = At.Shift.AFTER))
	private void iris$applyWeatherOverrides(LightTexture manager, float f, double d, double e, double g, CallbackInfo ci) {
		// TODO: This is a temporary workaround for https://github.com/IrisShaders/Iris/issues/219
		pipeline.pushProgram(GbufferProgram.WEATHER);
		pipeline.popProgram(GbufferProgram.WEATHER);
	}


	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$beginTranslucents(PoseStack poseStack, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightTexture lightTexture, Matrix4f matrix4f,
										CallbackInfo ci, ProfilerFiller profiler, Vec3 vec3, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										MultiBufferSource.BufferSource bufferSource) {
		profiler.popPush("iris_entity_draws");
		bufferSource.endBatch();

		profiler.popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}
}
