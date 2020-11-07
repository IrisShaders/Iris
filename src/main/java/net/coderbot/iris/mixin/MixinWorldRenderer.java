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
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinWorldRenderer {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String RENDER_SKY = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;F)V";
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V";
	private static final String POSITIVE_Y = "Lnet/minecraft/client/util/math/Vector3f;POSITIVE_Y:Lnet/minecraft/client/util/math/Vector3f;";
	private static final String PEEK = "Lnet/minecraft/client/util/math/MatrixStack;peek()Lnet/minecraft/client/util/math/MatrixStack$Entry;";

	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=terrain"))
	private void iris$setupTerrainShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferModelView(matrices.peek().getModel());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		try {
			Iris.getShaderManager().useTerrainShaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities"))
	private void iris$stopUsingTerrainShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		GlProgramManager.useProgram(0);
	}
	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=clouds"))
	private void iris$startUsingCloudShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci){
		try {
			Iris.getShaderManager().useCloudShaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=weather"))
	private void iris$stopUsingCloudShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci){
		GlProgramManager.useProgram(0);
	}
	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=outline"))
	private void iris$startUsingOutlineShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci){
		try {
			Iris.getShaderManager().useBasicShaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=string"))
	private void iris$stopUsingOutlineShaders(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci){
		GlProgramManager.useProgram(0);
	}

	@Inject(method = RENDER_SKY,
			slice = @Slice(from = @At(value = "FIELD", target = POSITIVE_Y)),
			at = @At(value = "INVOKE:FIRST", target = PEEK))
	private void iris$renderSky$postCelestialRotate(MatrixStack matrices, float tickDelta, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setCelestialModelView(matrices.peek().getModel().copy());
	}
}
