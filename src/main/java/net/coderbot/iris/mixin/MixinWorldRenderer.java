package net.coderbot.iris.mixin;

import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinWorldRenderer {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String RENDER_SKY = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;F)V";
	private static final String RENDER_LAYER = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V";
	private static final String RENDER_CLOUDS = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;FDDD)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.peek().getModel());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		Iris.getPipeline().beginWorldRender();
	}

	@Inject(method = RENDER, at = @At("RETURN"))
	private void iris$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Iris.getPipeline().endWorldRender();
		Iris.getCompositeRenderer().renderAll();
	}

	// TODO: end sky
	@Inject(method = RENDER_SKY, at = @At("HEAD"))
	private void iris$renderSky$begin(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		Iris.getPipeline().beginSky();
	}

	@Inject(method = RENDER_SKY,
		at = @At(value = "INVOKE:FIRST", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableFog()V"),
		slice = @Slice(from = @At(value = "FIELD:FIRST", target = "Lnet/minecraft/client/render/WorldRenderer;lightSkyBuffer:Lnet/minecraft/client/gl/VertexBuffer;"),
			to = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/render/SkyProperties;getFogColorOverride(FF)[F")))
	private void iris$renderSky$drawHorizon(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		new HorizonRenderer().renderHorizon(matrices);
	}

	@Inject(method = RENDER_SKY,
		at = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"))
	private void iris$renderSky$beginTextured(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		Iris.getPipeline().beginTexturedSky();
	}

	@Inject(method = RENDER_SKY,
		slice = @Slice(from = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V")),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;method_23787(F)F"))
	private void iris$renderSky$endTextured(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		Iris.getPipeline().endTexturedSky();
	}

	@Inject(method = RENDER_SKY, at = @At("RETURN"))
	private void iris$renderSky$end(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		Iris.getPipeline().endSky();
	}

	@Inject(method = RENDER_CLOUDS, at = @At("HEAD"))
	private void iris$beginClouds(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		Iris.getPipeline().beginClouds();
	}

	@Inject(method = RENDER_CLOUDS, at = @At("RETURN"))
	private void iris$endClouds(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		Iris.getPipeline().endClouds();
	}

	@Inject(method = RENDER_LAYER, at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderLayer renderLayer, MatrixStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		Iris.getPipeline().beginTerrainLayer(renderLayer);
	}

	@Inject(method = RENDER_LAYER, at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderLayer renderLayer, MatrixStack matrixStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		Iris.getPipeline().endTerrainLayer(renderLayer);
	}

	@Inject(method = "renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", at = @At("HEAD"))
	private void iris$beginWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo callback) {
		Iris.getPipeline().beginWeather();
	}

	@Inject(method = "renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", at = @At("RETURN"))
	private void iris$endWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo callback) {
		Iris.getPipeline().endWeather();
	}

	@Inject(method = "renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
	private void iris$beginWorldBorder(Camera camera, CallbackInfo callback) {
		Iris.getPipeline().beginWorldBorder();
	}

	@Inject(method = "renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", at = @At("RETURN"))
	private void iris$endWorldBorder(Camera camera, CallbackInfo callback) {
		Iris.getPipeline().endWorldBorder();
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

	@Inject(method = RENDER, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;transparencyShader:Lnet/minecraft/client/gl/ShaderEffect;"))
	private void iris$copyCurrentDepthTexture(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Iris.getPipeline().copyCurrentDepthTexture();
	}
}
