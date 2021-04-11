package net.coderbot.iris.mixin;

import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.FlushableVertexConsumerProvider;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;
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
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;F)V";
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
		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		
		pipeline.beginWorldRendering();
	}

	// Inject a bit early so that we can end our rendering in time.
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;method_23792()V"))
	private void iris$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.finalizeWorldRendering();
		pipeline = null;
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = CLEAR))
	private void iris$beforeClear(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
	private void iris$afterClear(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_SKY))
	private void iris$beginSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.SKY_TEXTURED);
		skyTextureEnabled = true;
	}

	@Redirect(method = RENDER, at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;viewDistance:I"),
	          slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V")))
	private int iris$alwaysRenderSky(GameOptions options) {
		return Math.max(options.viewDistance, 4);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V"))
	private void iris$renderSky$disableTexture(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		if (skyTextureEnabled) {
			skyTextureEnabled = false;
			pipeline.pushProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = RENDER_SKY,
		at = @At(value = "INVOKE:FIRST", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableFog()V"),
		slice = @Slice(from = @At(value = "FIELD:FIRST", target = "Lnet/minecraft/client/render/WorldRenderer;lightSkyBuffer:Lnet/minecraft/client/gl/VertexBuffer;"),
			to = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/render/SkyProperties;getFogColorOverride(FF)[F")))
	private void iris$renderSky$drawHorizon(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		new HorizonRenderer().renderHorizon(matrices);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F"),
		slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/util/math/Vector3f;POSITIVE_Y:Lnet/minecraft/client/util/math/Vector3f;")))
	private void iris$renderSky$tiltSun(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(pipeline.getSunPathRotation()));
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V"))
	private void iris$renderSky$enableTexture(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		if (!skyTextureEnabled) {
			skyTextureEnabled = true;
			pipeline.popProgram(GbufferProgram.SKY_BASIC);
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.SKY_TEXTURED);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLOUDS);
	}

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
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.WEATHER);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER))
	private void iris$beginWorldBorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WORLD_BORDER, shift = At.Shift.AFTER))
	private void iris$endWorldBorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.TEXTURED_LIT);
	}

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
		if (immediate instanceof FlushableVertexConsumerProvider) {
			((FlushableVertexConsumerProvider) immediate).flushNonTranslucentContent();
		}

		pipeline.beginTranslucents();
	}
}
