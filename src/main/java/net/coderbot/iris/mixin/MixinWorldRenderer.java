package net.coderbot.iris.mixin;

import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.FlushableVertexConsumerProvider;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinWorldRenderer {
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V";
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLjava/lang/Runnable;)V";
	private static final String RENDER_LAYER = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/util/math/MatrixStack;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V";
	private static final String RENDER_WORLD_BORDER = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V";

	@Unique
	private boolean skyTextureEnabled;

	@Unique
	private WorldRenderingPipeline pipeline;

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.peek().getModel());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension(), true);

		if (pipeline instanceof DeferredWorldRenderingPipeline) {
			((DeferredWorldRenderingPipeline) pipeline).getUpdateNotifier().onNewFrame();
		}

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			((CoreWorldRenderingPipeline) pipeline).getUpdateNotifier().onNewFrame();
		}

		pipeline.beginWorldRendering();
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	// Inject a bit early so that we can end our rendering in time.
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;method_23792()V"))
	private void iris$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		MinecraftClient.getInstance().getProfiler().swap("iris_final");
		pipeline.finalizeWorldRendering();
		pipeline.setPhase(WorldRenderingPhase.NOT_RENDERING_WORLD);
		pipeline = null;
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;updateChunks(J)V", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.renderShadows((WorldRendererAccessor) this, camera);
	}

	// TODO(21w10a): Deal with render hooks
	/*@Inject(method = RENDER, at = @At(value = "INVOKE", target = CLEAR))
	private void iris$beforeClear(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
	private void iris$afterClear(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLEAR);
	}*/

	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=sky"))
	private void iris$beginSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	@Redirect(method = RENDER, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V")))
	private int iris$alwaysRenderSky(GameOptions options) {
		return Math.max(options.viewDistance, 4);
	}

	// TODO(21w10a): Restore sky render hooks
	/*
	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V"))
	private void iris$renderSky$disableTexture(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		if (skyTextureEnabled) {
			skyTextureEnabled = false;
			pipeline.pushProgram(GbufferProgram.SKY_BASIC);
		}
	}*/

	@Inject(method = RENDER_SKY,
		at = @At(value = "INVOKE", target = "net/minecraft/client/gl/VertexBuffer.setShader (Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V", shift = At.Shift.AFTER),
		slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SkyProperties;getFogColorOverride(FF)[F")))
	private void iris$renderSky$drawHorizon(MatrixStack matrices, Matrix4f projectionMatrix, float f, Runnable runnable, CallbackInfo callback) {
		new HorizonRenderer().renderHorizon(matrices.peek().getModel().copy(), projectionMatrix.copy(), GameRenderer.getPositionShader());
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F"),
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3f;POSITIVE_Y:Lnet/minecraft/util/math/Vec3f;")))
	private void iris$renderSky$tiltSun(MatrixStack matrices, Matrix4f projectionMatrix, float f, Runnable runnable, CallbackInfo callback) {
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(pipeline.getSunPathRotation()));
	}

	/*@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V"))
	private void iris$renderSky$enableTexture(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		if (!skyTextureEnabled) {
			skyTextureEnabled = true;
			pipeline.popProgram(GbufferProgram.SKY_BASIC);
		}
	}*/

	// TODO(21w10a): Deal with render hooks
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		// After the sky has rendered, there isn't a clear phase.
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	/*@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void iris$maybeRemoveClouds(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (!pipeline.shouldRenderClouds()) {
			ci.cancel();
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLOUDS);
	}*/

	// TODO(21w10a): Restore render layer hooks
	/*
	@Inject(method = RENDER_LAYER, at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderLayer renderLayer, MatrixStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderLayer == RenderLayer.getSolid() || renderLayer == RenderLayer.getCutout() || renderLayer == RenderLayer.getCutoutMipped()) {
			pipeline.pushProgram(GbufferProgram.TERRAIN);
		} else if (renderLayer == RenderLayer.getTranslucent() || renderLayer == RenderLayer.getTripwire()) {
			pipeline.pushProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderLayer);
		}
	}

	@Inject(method = RENDER_LAYER, at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderLayer renderLayer, MatrixStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderLayer == RenderLayer.getSolid() || renderLayer == RenderLayer.getCutout() || renderLayer == RenderLayer.getCutoutMipped()) {
			pipeline.popProgram(GbufferProgram.TERRAIN);
		} else if (renderLayer == RenderLayer.getTranslucent() || renderLayer == RenderLayer.getTripwire()) {
			pipeline.popProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderLayer);
		}
	}*/

	// TODO(21w10a): Deal with render hooks
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.WEATHER);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	/*@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER))
	private void iris$beginWorldBorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER, shift = At.Shift.AFTER))
	private void iris$endWorldBorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.TEXTURED_LIT);
	}*/

	// TODO: Need to figure out how to properly track these values (https://github.com/IrisShaders/Iris/issues/19)
	/*@Inject(method = "renderEntity", at = @At("HEAD"))
	private void iris$beginEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(entity);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;getPos()Lnet/minecraft/util/math/BlockPos;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$getCurrentBlockEntity(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3, VertexConsumerProvider.Immediate immediate, Set var39, Iterator var40, BlockEntity blockEntity2){
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(blockEntity2);
	}*/

	@Inject(method = RENDER, at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$beginTranslucents(MatrixStack matrices, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
										CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										VertexConsumerProvider.Immediate immediate) {
		profiler.swap("iris_opaque_entity_draws");

		if (immediate instanceof FlushableVertexConsumerProvider) {
			((FlushableVertexConsumerProvider) immediate).flushNonTranslucentContent();
		}

		profiler.swap("iris_pre_translucent");
		pipeline.beginTranslucents();
	}

	@Inject(method = RENDER, at = @At(value = "CONSTANT", args = "stringValue=blockentities"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$StartBlockEntities(MatrixStack matrices, float tickDelta, long limitTime,
										 boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										 LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
										 CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f,
										 Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										 VertexConsumerProvider.Immediate immediate) {
		pipeline.setPhase(WorldRenderingPhase.BLOCK_ENTITIES);
	}

	@Inject(method = RENDER, at = @At(value = "CONSTANT", args = "stringValue=destroyProgress"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$EndBlockEntities(MatrixStack matrices, float tickDelta, long limitTime,
									   boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
									   LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
									   CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f,
									   Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
									   VertexConsumerProvider.Immediate immediate) {
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}
}
