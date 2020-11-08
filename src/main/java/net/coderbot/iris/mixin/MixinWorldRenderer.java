package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.GlProgramManager;
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
	private static final String POSITIVE_Y = "Lnet/minecraft/client/util/math/Vector3f;POSITIVE_Y:Lnet/minecraft/client/util/math/Vector3f;";
	private static final String PEEK = "Lnet/minecraft/client/util/math/MatrixStack;peek()Lnet/minecraft/client/util/math/MatrixStack$Entry;";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$captureRenderingState(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.peek().getModel());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
	}

	@Inject(method = RENDER_SKY,
			slice = @Slice(from = @At(value = "FIELD", target = POSITIVE_Y)),
			at = @At(value = "INVOKE:FIRST", target = PEEK))
	private void iris$renderSky$postCelestialRotate(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setCelestialModelView(matrices.peek().getModel().copy());
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
}
