package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	// TODO: This probably won't be compatible with mods that directly mess with the GL projection matrix.
	// https://github.com/jellysquid3/sodium-fabric/blob/1df506fd39dac56bb410725c245e6e51208ec732/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/shader/ChunkProgram.java#L56
	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage bufferBuilderStorage, CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GlDebugInfo.getCpuInfo());
		Iris.logger.info("GPU: " + GlDebugInfo.getRenderer() + " (Supports OpenGL " + GlDebugInfo.getVersion() + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name"));
	}

	@Inject(method = "loadProjectionMatrix(Lnet/minecraft/util/math/Matrix4f;)V", at = @At("HEAD"))
	private void iris$captureProjectionMatrix(Matrix4f projectionMatrix, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferProjection(projectionMatrix);
	}

	@Inject(method = "render(FJZ)V", at = @At("HEAD"))
	private void iris$beginFrame(float tickDelta, long startTime, boolean tick, CallbackInfo callback) {
		SystemTimeUniforms.COUNTER.beginFrame();
		SystemTimeUniforms.TIMER.beginFrame(startTime);
	}
}
