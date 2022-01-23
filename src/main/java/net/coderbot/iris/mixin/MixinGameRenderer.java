package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.FixedFunctionWorldRenderingPipeline;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;

import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	@Shadow
	private boolean renderHand;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(Minecraft client, ResourceManager resourceManager, RenderBuffers bufferBuilderStorage,
								CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GlUtil.getCpuInfo());
		Iris.logger.info("GPU: " + GlUtil.getRenderer() + " (Supports OpenGL " + GlUtil.getOpenGLVersion() + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
	}

	@Redirect(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V"))
	private void disableVanillaHandRendering(ItemInHandRenderer itemInHandRenderer, float tickDelta, PoseStack poseStack, BufferSource bufferSource, LocalPlayer localPlayer, int light) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		itemInHandRenderer.renderHandsWithItems(tickDelta, poseStack, bufferSource, localPlayer, light);
	}

	//TODO: check cloud phase

	@Inject(method = "getPositionShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isSky()) {
			override(ShaderKey.SKY_BASIC, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasic
		} else if (isRenderingWorld()) {
			override(ShaderKey.BASIC, cir);
		}
	}

	@Inject(method = "getPositionColorShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionColorShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isSky()) {
			override(ShaderKey.SKY_BASIC_COLOR, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowBasicColor
		} else if (isRenderingWorld()) {
			override(ShaderKey.BASIC_COLOR, cir);
		}
	}

	// TODO: getPositionColorTexShader

	@Inject(method = "getPositionTexShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isSky()) {
			override(ShaderKey.SKY_TEXTURED, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowTextured
		} else if (isRenderingWorld()) {
			override(ShaderKey.TEXTURED, cir);
		}
	}

	@Inject(method = "getPositionTexColorShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isSky()) {
			override(ShaderKey.SKY_TEXTURED_COLOR, cir);
		} else if (ShadowRenderer.ACTIVE) {
			// TODO: shadowTexturedColor
		} else if (isRenderingWorld()) {
			override(ShaderKey.TEXTURED_COLOR, cir);
		}
	}

	// TODO: getBlockShader, getNewEntityShader

	@Inject(method = {
			"getParticleShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideParticleShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if(isPhase(WorldRenderingPhase.RAIN_SNOW)) {
			override(ShaderKey.WEATHER, cir);
		} else if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(ShaderKey.PARTICLES, cir);
		}
		// TODO: shadows
	}

	// TODO: getPositionColorLightmapShader, getPositionColorTexLightmapShader

	@Inject(method = "getPositionTexColorNormalShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overridePositionTexColorNormalShader(CallbackInfoReturnable<ShaderInstance> cir) {
		override(ShaderKey.CLOUDS, cir);
		// TODO: shadows
	}

	// TODO: getPositionTexLightmapColorShader

	@Inject(method = "getRendertypeSolidShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideSolidShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_TERRAIN_CUTOUT, cir);
		} else {
			override(ShaderKey.TERRAIN_SOLID, cir);
		}
	}

	@Inject(method = "getRendertypeCutoutMippedShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutMippedShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_TERRAIN_CUTOUT, cir);
		} else {
			override(ShaderKey.TERRAIN_CUTOUT_MIPPED, cir);
		}
	}

	@Inject(method = "getRendertypeCutoutShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCutoutShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(ShaderKey.SHADOW_TERRAIN_CUTOUT, cir);
		} else {
			override(ShaderKey.TERRAIN_CUTOUT, cir);
		}
	}

	@Inject(method = "getRendertypeTranslucentShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTranslucentShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_TERRAIN_CUTOUT, cir);
		} else {
			override(ShaderKey.TERRAIN_TRANSLUCENT, cir);
		}
	}

	// getRenderTypeTranslucentMovingBlockShader, getRenderTypeTranslucentNoCrumblingShader

	@Inject(method = "getRendertypeTripwireShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTripwireShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_TERRAIN_CUTOUT, cir);
		} else {
			override(ShaderKey.TERRAIN_TRANSLUCENT, cir);
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
			"getRendertypeArmorCutoutNoCullShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityCutoutShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_ENTITIES_CUTOUT, cir);
		} else if (HandRenderer.INSTANCE.isActive()) {
			override(HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(ShaderKey.BLOCK_ENTITY_DIFFUSE, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.ENTITIES_CUTOUT_DIFFUSE, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEnergySwirlShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEnergySwirlShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_ENTITIES_CUTOUT, cir);
		} else if (HandRenderer.INSTANCE.isActive()) {
			override(HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT : ShaderKey.HAND_TRANSLUCENT, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(ShaderKey.BLOCK_ENTITY, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.ENTITIES_CUTOUT, cir);
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
			override(ShaderKey.GLINT, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEntitySolidShader",
			"getRendertypeEntityNoOutlineShader",
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntitySolidDiffuseShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_ENTITIES_CUTOUT, cir);
		} else if (HandRenderer.INSTANCE.isActive()) {
			override(HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT_DIFFUSE : ShaderKey.HAND_WATER_DIFFUSE, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(ShaderKey.BLOCK_ENTITY_DIFFUSE, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.ENTITIES_SOLID_DIFFUSE, cir);
		}
	}

	@Inject(method = {
			"getRendertypeWaterMaskShader",
			"getRendertypeEntityShadowShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntitySolidShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_ENTITIES_CUTOUT, cir);
		} else if (HandRenderer.INSTANCE.isActive()) {
			override(HandRenderer.INSTANCE.isRenderingSolid() ? ShaderKey.HAND_CUTOUT : ShaderKey.HAND_TRANSLUCENT, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(ShaderKey.BLOCK_ENTITY, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.ENTITIES_SOLID, cir);
		}
	}

	@Inject(method = "getRendertypeBeaconBeamShader", at = @At("HEAD"), cancellable = true)
	private static void iris$overrideBeaconBeamShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(ShaderKey.SHADOW_BEACON_BEAM, cir);
		} else {
			override(ShaderKey.BEACON, cir);
		}
	}

	// TODO: getRenderTypeEntityDecalShader (uses entity diffuse lighting)
	// TODO: getRenderTypeEntityAlphaShader (weird alpha test behavior!!!)

	// NOTE: getRenderTypeOutlineShader should not be overriden.

	@Inject(method = {
			"getRendertypeEyesShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEntityEyesShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			// TODO: Wrong program
			override(ShaderKey.SHADOW_ENTITIES_CUTOUT, cir);
		} else if (GbufferPrograms.isRenderingBlockEntities()) {
			override(ShaderKey.BLOCK_ENTITY, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.ENTITIES_EYES, cir);
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
			override(ShaderKey.LEASH, cir);
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
			override(ShaderKey.LIGHTNING, cir);
		}
	}

	@Inject(method = {
			"getRendertypeCrumblingShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideCrumblingShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(ShaderKey.CRUMBLING, cir);
		}
	}

	@Inject(method = {
			"getRendertypeTextShader",
			"getRendertypeTextSeeThroughShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTextShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(ShaderKey.TEXT, cir);
		}
	}

	@Inject(method = {
			"getRendertypeTextIntensityShader",
			"getRendertypeTextIntensitySeeThroughShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideTextIntensityShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (isRenderingWorld() && !ShadowRenderer.ACTIVE) {
			override(ShaderKey.TEXT_INTENSITY, cir);
		}
	}

	@Inject(method = {
			"getRendertypeEndGatewayShader",
			"getRendertypeEndPortalShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideEndPortalShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if(!ShadowRenderer.ACTIVE) {
			override(ShaderKey.BLOCK_ENTITY, cir);
		}
	}

	@Inject(method = {
			"getRendertypeLinesShader"
	}, at = @At("HEAD"), cancellable = true)
	private static void iris$overrideLinesShader(CallbackInfoReturnable<ShaderInstance> cir) {
		if (ShadowRenderer.ACTIVE) {
			override(ShaderKey.SHADOW_LINES, cir);
		} else if (isRenderingWorld()) {
			override(ShaderKey.LINES, cir);
		}
	}

	private static boolean isSky() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			switch (pipeline.getPhase()) {
				case SKY:
				case SUNSET:
				case SUN:
				case STARS:
				case VOID:
				case MOON:
					return true;
				default: return false;
			}
		} else {
			return false;
		}
	}

	private static boolean isPhase(WorldRenderingPhase phase) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			return pipeline.getPhase() == phase;
		} else {
			return false;
		}
	}

	private static boolean isRenderingWorld() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).isRenderingWorld();
		} else {
			return false;
		}
	}

	private static void override(ShaderKey key, CallbackInfoReturnable<ShaderInstance> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			ShaderInstance override = ((CoreWorldRenderingPipeline) pipeline).getShaderMap().getShader(key);

			if (override != null) {
				cir.setReturnValue(override);
			}
		}
	}
}
