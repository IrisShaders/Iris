package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlUtil;
import net.coderbot.iris.Iris;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(Minecraft client, ResourceManager resourceManager, RenderBuffers bufferBuilderStorage,
								CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GlUtil.getCpuInfo());
		Iris.logger.info("GPU: " + GlUtil.getRenderer() + " (Supports OpenGL " + GlUtil.getOpenGLVersion() + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name"));
	}

	@Inject(method = "getPositionShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyBasic, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasic
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getBasic, cir);
		}
	}

	@Inject(method = "getPositionColorShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionColorShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyBasicColor, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasicColor
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getBasicColor, cir);
		}
	}

	// TODO: getPositionColorTexShader

	@Inject(method = "getPositionTexShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isPhase(WorldRenderingPhase.SKY)) {
			override(CoreWorldRenderingPipeline::getSkyTextured, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowTextured
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getTextured, cir);
		}
	}

	@Inject(method = "getPositionTexColorShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorShader(CallbackInfoReturnable<ShaderInstance> cir) {
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
			"getParticleShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideParticleShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if(isPhase(WorldRenderingPhase.WEATHER)) {
			override(CoreWorldRenderingPipeline::getWeather, cir);
		} else if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getParticles, cir);
		}
		// TODO: shadows
	}

	// TODO: getPositionColorLightmapShader, getPositionColorTexLightmapShader

	@Inject(method = "getPositionTexColorNormalShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorNormalShader(CallbackInfoReturnable<ShaderInstance> cir) {
		override(CoreWorldRenderingPipeline::getClouds, cir);
		// TODO: shadows
	}

	// TODO: getPositionTexLightmapColorShader

	@Inject(method = "getRendertypeSolidShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideSolidShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrain, cir);
		}
	}

	@Inject(method = "getRendertypeCutoutMippedShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutMippedShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrainCutoutMipped, cir);
		}
	}

	@Inject(method = "getRendertypeCutoutShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTerrainCutout, cir);
		}
	}

	@Inject(method = "getRendertypeTranslucentShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTranslucentShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTranslucent, cir);
		}
	}

	// getRenderTypeTranslucentMovingBlockShader, getRenderTypeTranslucentNoCrumblingShader

	@Inject(method = "getRendertypeTripwireShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTripwireShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
		} else {
			override(CoreWorldRenderingPipeline::getTranslucent, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEntityCutoutShader",
			"getRendertypeEntityCutoutNoCullShader",
			"getRendertypeEntityCutoutNoCullZOffsetShader",
			"getRendertypeEntitySmoothCutoutShader",
			"getRendertypeEntityTranslucentShader",
			"getRendertypeEntityTranslucentCullShader",
			"getRendertypeItemEntityTranslucentCullShader",
			"getRendertypeArmorCutoutNoCullShader",
			"getRendertypeEnergySwirlShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityCutoutShader(CallbackInfoReturnable<ShaderInstance> cir) {
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
			"getRendertypeGlintShader",
			"getRendertypeGlintDirectShader",
			"getRendertypeGlintTranslucentShader",
			"getRendertypeArmorGlintShader",
			"getRendertypeEntityGlintDirectShader",
			"getRendertypeEntityGlintShader",
			"getRendertypeArmorEntityGlintShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideGlintShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if(isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getGlint, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEntitySolidShader",
			"getRendertypeWaterMaskShader",
			"getRendertypeEntityNoOutlineShader",
			"getRendertypeEntityShadowShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntitySolidShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(CoreWorldRenderingPipeline::getShadowEntitiesCutout, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getEntitiesSolid, cir);
		}
	}

	@Inject(method = "getRendertypeBeaconBeamShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideBeaconBeamShader(CallbackInfoReturnable<ShaderInstance> cir) {
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
			"getRendertypeEyesShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityEyesShader(CallbackInfoReturnable<ShaderInstance> cir) {
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
			"getRendertypeLeashShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLeashShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			// override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
			return;
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getLeash, cir);
		}
	}

	@Inject(method = {
			"getRendertypeLightningShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLightningShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			// override(CoreWorldRenderingPipeline::getShadowTerrainCutout, cir);
			return;
		} else if (isRenderingWorld()) {
			override(CoreWorldRenderingPipeline::getLightning, cir);
		}
	}

	@Inject(method = {
			"getRendertypeCrumblingShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCrumblingShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getCrumbling, cir);
		}
	}

	@Inject(method = {
			"getRendertypeTextShader",
			"getRendertypeTextSeeThroughShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTextShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getText, cir);
		}
	}

	@Inject(method = {
			"getRendertypeTextIntensityShader",
			"getRendertypeTextIntensitySeeThroughShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTextIntensityShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getTextIntensity, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEndGatewayShader",
			"getRendertypeEndPortalShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEndPortalShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if(!ShadowRenderer.ACTIVE) {
			override(CoreWorldRenderingPipeline::getBlock, cir);
		}
	}

	@Inject(method = {
			"getRendertypeLinesShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLinesShader(CallbackInfoReturnable<ShaderInstance> cir) {
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

	private static void override(Function<CoreWorldRenderingPipeline, ShaderInstance> getter, CallbackInfoReturnable<ShaderInstance> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			ShaderInstance override = getter.apply(((CoreWorldRenderingPipeline) pipeline));

			if (override != null) {
				cir.setReturnValue(override);
			}
		}
	}
}
