package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.client.render.Shader;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage bufferBuilderStorage, CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GlDebugInfo.getCpuInfo());
		Iris.logger.info("GPU: " + GlDebugInfo.getRenderer() + " (Supports OpenGL " + GlDebugInfo.getVersion() + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name"));
	}

	// TODO: This probably won't be compatible with mods that directly mess with the GL projection matrix.
	// https://github.com/jellysquid3/sodium-fabric/blob/1df506fd39dac56bb410725c245e6e51208ec732/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/shader/ChunkProgram.java#L56
	@Inject(method = "loadProjectionMatrix(Lnet/minecraft/util/math/Matrix4f;)V", at = @At("HEAD"))
	private void iris$captureProjectionMatrix(Matrix4f projectionMatrix, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferProjection(projectionMatrix);
	}

	@Inject(method = "render(FJZ)V", at = @At("HEAD"))
	private void iris$beginFrame(float tickDelta, long startTime, boolean tick, CallbackInfo callback) {
		SystemTimeUniforms.COUNTER.beginFrame();
		SystemTimeUniforms.TIMER.beginFrame(startTime);
	}

	@Inject(method = "getPositionShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyBasic, cir);
		}
	}

	@Inject(method = "getPositionColorShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionColorShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyBasicColor, cir);
		}
	}

	@Inject(method = "getPositionTexShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyTextured, cir);
		}
	}

	@Inject(method = "getRenderTypeSolidShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideSolidShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getTerrain, cir);
	}

	@Inject(method = "getRenderTypeCutoutShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getTerrainCutout, cir);
	}

	@Inject(method = "getRenderTypeCutoutMippedShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutMippedShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getTerrainCutoutMipped, cir);
	}

	@Inject(method = "getRenderTypeTranslucentShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTranslucentShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getTranslucent, cir);
	}

	@Inject(method = "getRenderTypeTripwireShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTripwireShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getTranslucent, cir);
	}

	private static boolean isPhase(WorldRenderingPhase phase) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).getPhase() == phase;
		} else {
			return false;
		}
	}

	private static void override(Function<CoreWorldRenderingPipeline, Shader> getter, CallbackInfoReturnable<Shader> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			Shader override = getter.apply(((CoreWorldRenderingPipeline) pipeline));

			if (override != null) {
				cir.setReturnValue(override);
			}
		}
	}
}
