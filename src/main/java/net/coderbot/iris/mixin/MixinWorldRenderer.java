package net.coderbot.iris.mixin;

import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinWorldRenderer {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String RENDER_SKY = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;F)V";
	private static final String RENDER_LAYER = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V";
	private static final String RENDER_CLOUDS = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;FDDD)V";
	private static final String POSITIVE_Y = "Lnet/minecraft/client/util/math/Vector3f;POSITIVE_Y:Lnet/minecraft/client/util/math/Vector3f;";
	private static final String PEEK = "Lnet/minecraft/client/util/math/MatrixStack;peek()Lnet/minecraft/client/util/math/MatrixStack$Entry;";
	private static final String DRAW_OUTLINE = "Lnet/minecraft/client/render/WorldRenderer;drawShapeOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/util/shape/VoxelShape;DDDFFFF)V";
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V";
	private static final String GET_POS = "Lnet/minecraft/block/entity/BlockEntity;getPos()Lnet/minecraft/util/math/BlockPos;";
	private static final String RENDER_ENTITY = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.peek().getModel());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		Iris.getPipeline().beginWorldRender();
	}

	@Inject(method = RENDER, at = @At("RETURN"))
	private void iris$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Iris.getPipeline().endWorldRender();
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



}
