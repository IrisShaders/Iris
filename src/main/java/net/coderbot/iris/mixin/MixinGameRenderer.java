package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.ShadowRenderer;
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
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasic
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getBasic, cir);
		}
	}

	@Inject(method = "getPositionColorShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionColorShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyBasicColor, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasicColor
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getBasicColor, cir);
		}
	}

	// TODO: getPositionColorTexShader

	@Inject(method = "getPositionTexShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyTextured, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowTextured
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getTextured, cir);
		}
	}

	@Inject(method = "getPositionTexColorShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorShader(CallbackInfoReturnable<Shader> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyTexturedColor, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowTexturedColor
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getTexturedColor, cir);
		}
	}

	// TODO: getBlockShader, getNewEntityShader

	@Inject(method = {
			"getParticleShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideParticleShader(CallbackInfoReturnable<Shader> cir) {
		if(isPhase(WorldRenderingPhase.WEATHER)) {
			override(CoreWorldRenderingPipeline::getWeather, cir);
		} else if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getParticles, cir);
		}
		// TODO: shadows
	}

	// TODO: getPositionColorLightmapShader, getPositionColorTexLightmapShader

	@Inject(method = "getPositionTexColorNormalShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorNormalShader(CallbackInfoReturnable<Shader> cir) {
		override(CoreWorldRenderingPipeline::getClouds, cir);
		// TODO: shadows
	}

	// TODO: getPositionTexLightmapColorShader

	@Inject(method = "getRenderTypeSolidShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideSolidShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrain, cir);
		}
	}

	@Inject(method = "getRenderTypeCutoutMippedShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutMippedShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrainCutoutMipped, cir);
		}
	}

	@Inject(method = "getRenderTypeCutoutShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrainCutout, cir);
		}
	}

	@Inject(method = "getRenderTypeTranslucentShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTranslucentShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTranslucent, cir);
		}
	}

	// getRenderTypeTranslucentMovingBlockShader, getRenderTypeTranslucentNoCrumblingShader

	@Inject(method = "getRenderTypeTripwireShader()Lnet/minecraft/client/render/Shader;", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTripwireShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTranslucent, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeEntityCutoutShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityCutoutNoNullShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityCutoutNoNullZOffsetShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntitySmoothCutoutShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityTranslucentShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityTranslucentCullShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeItemEntityTranslucentCullShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeArmorCutoutNoCullShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEnergySwirlShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityCutoutShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowEntitiesCutout, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getEntitiesCutout, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeGlintShader",
			"getRenderTypeGlintDirectShader",
			"getRenderTypeGlintTranslucentShader",
			"getRenderTypeArmorGlintShader",
			"getRenderTypeEntityGlintDirectShader",
			"getRenderTypeEntityGlintShader",
			"getRenderTypeArmorEntityGlintShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideGlintShader(CallbackInfoReturnable<Shader> cir) {
		if(isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getGlint, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeEntitySolidShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeWaterMaskShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityNoOutlineShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeEntityShadowShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntitySolidShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowEntitiesCutout, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getEntitiesSolid, cir);
		}
	}

	@Inject(method = "getRenderTypeBeaconBeamShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideBeaconBeamShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getShadowBeaconBeam, cir);
		} else {
			override(CoreWorldRenderingPipeline::getBeacon, cir);
		}
	}

	// TODO: getRenderTypeEntityDecalShader
	// TODO: getRenderTypeEntityAlphaShader (weird alpha test behavior!!!)

	// NOTE: getRenderTypeOutlineShader should not be overriden.

	@Inject(method = {
			"getRenderTypeEyesShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityEyesShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowEntitiesCutout, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getEntitiesEyes, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeLeashShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLeashShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			// override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
			return;
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getLeash, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeLightningShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLightningShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			// override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
			return;
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getLightning, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeCrumblingShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCrumblingShader(CallbackInfoReturnable<Shader> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getCrumbling, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeTextShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeTextIntensityShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeTextSeeThroughShader()Lnet/minecraft/client/render/Shader;",
			"getRenderTypeTextIntensitySeeThroughShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTextShader(CallbackInfoReturnable<Shader> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getText, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeEndGatewayShader",
			"getRenderTypeEndPortalShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEndPortalShader(CallbackInfoReturnable<Shader> cir) {
		if(!ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		}
	}

	@Inject(method = {
			"getRenderTypeLinesShader()Lnet/minecraft/client/render/Shader;"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLinesShader(CallbackInfoReturnable<Shader> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getShadowLines, cir);
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getLines, cir);
		}
	}

	private static boolean isPhase(WorldRenderingPhase phase) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).getPhase() == phase;
		} else {
			return false;
		}
	}

	private static boolean isRenderingWorld() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).getPhase() != WorldRenderingPhase.NOT_RENDERING_WORLD;
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
