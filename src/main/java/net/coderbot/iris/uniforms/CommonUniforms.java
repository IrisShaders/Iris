package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureInfoCache.TextureInfo;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.uniforms.transforms.SmoothedVec2f;
import net.coderbot.iris.vendored.joml.Math;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;
import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.IntSupplier;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class CommonUniforms {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Vector2i ZERO_VECTOR_2i = new Vector2i();
	private static final Vector4i ZERO_VECTOR_4i = new Vector4i(0, 0, 0, 0);
	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addDynamicUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		ExternallyManagedUniforms.addExternallyManagedUniforms117(uniforms);
		FogUniforms.addFogUniforms(uniforms, fogMode);
		IrisInternalUniforms.addFogUniforms(uniforms, fogMode);

		// This is a fallback for when entityId via attributes cannot be used. (lightning)
		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity, StateUpdateNotifiers.fallbackEntityNotifier);

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		//
		// Note: on 1.17+ we don't need to reset this when textures are bound, since
		// the shader will always be setup (and therefore uniforms will be re-uploaded)
		// after the texture is changed and before rendering starts.
		uniforms.uniform2i("atlasSize", () -> {
			int glId = RenderSystem.getShaderTexture(0);

			AbstractTexture texture = TextureTracker.INSTANCE.getTexture(glId);
			if (texture instanceof TextureAtlas) {
				TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
				return new Vector2i(info.getWidth(), info.getHeight());
			}

			return ZERO_VECTOR_2i;
		}, listener -> {});

		uniforms.uniform2i("gtextureSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].binding;

			TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
			return new Vector2i(info.getWidth(), info.getHeight());

		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform4i("blendFunc", () -> {
			GlStateManager.BlendState blend = GlStateManagerAccessor.getBLEND();

			if (((BooleanStateAccessor) blend.mode).isEnabled()) {
				return new Vector4i(blend.srcRgb, blend.dstRgb, blend.srcAlpha, blend.dstAlpha);
			} else {
				return ZERO_VECTOR_4i;
			}
		}, StateUpdateNotifiers.blendFuncNotifier);

		uniforms.uniform1i("renderStage", () -> GbufferPrograms.getCurrentPhase().ordinal(), StateUpdateNotifiers.phaseChangeNotifier);
	}

	public static void addCommonUniforms(DynamicUniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, FogMode fogMode) {
		CommonUniforms.addNonDynamicUniforms(uniforms, idMap, directives, updateNotifier);
		CommonUniforms.addDynamicUniforms(uniforms, fogMode);
	}

	public static void addNonDynamicUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.addCameraUniforms(uniforms, updateNotifier);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		BiomeParameters.addBiomeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IrisExclusiveUniforms.addIrisExclusiveUniforms(uniforms);
		IrisTimeUniforms.addTimeUniforms(uniforms);
		MatrixUniforms.addMatrixUniforms(uniforms, directives);
		IdMapUniforms.addIdMapUniforms(updateNotifier, uniforms, idMap, directives.isOldHandLight());
		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier, directives);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives) {
		ExternallyManagedUniforms.addExternallyManagedUniforms117(uniforms);

		SmoothedVec2f eyeBrightnessSmooth = new SmoothedVec2f(directives.getEyeBrightnessHalfLife(), directives.getEyeBrightnessHalfLife(), CommonUniforms::getEyeBrightness, updateNotifier);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hideGui)
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1f(PER_FRAME, "darknessFactor", CommonUniforms::getDarknessFactor)
			.uniform1f(PER_FRAME, "darknessLightFactor", CapturedRenderingState.INSTANCE::getDarknessLightFactor)
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1b(PER_FRAME, "is_sneaking", CommonUniforms::isSneaking)
			.uniform1b(PER_FRAME, "is_sprinting", CommonUniforms::isSprinting)
			.uniform1b(PER_FRAME, "is_hurt", CommonUniforms::isHurt)
			.uniform1b(PER_FRAME, "is_invisible", CommonUniforms::isInvisible)
			.uniform1b(PER_FRAME, "is_burning", CommonUniforms::isBurning)
			.uniform1b(PER_FRAME, "is_on_ground", CommonUniforms::isOnGround)
			// TODO: Do we need to clamp this to avoid fullbright breaking shaders? Or should shaders be able to detect
			//       that the player is trying to turn on fullbright?
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma().get())
			// just a dummy value for shaders where entityColor isn't supplied through a vertex attribute (and thus is
			// not available) - suppresses warnings. See AttributeShaderTransformer for the actual entityColor code.
			.uniform4f(ONCE, "entityColor", () -> new Vector4f(0, 0, 0, 0))
			.uniform1i(ONCE, "blockEntityId", () -> -1)
			.uniform1i(ONCE, "currentRenderedItemId", () -> -1)
			.uniform1f(ONCE, "pi", () -> Math.PI)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", () -> {
				Vector2f smoothed = eyeBrightnessSmooth.get();
				return new Vector2i((int) smoothed.x(),(int) smoothed.y());
			})
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(directives.getWetnessHalfLife(), directives.getDrynessHalfLife(), CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor);
	}

	private static boolean isOnGround() {
		return client.player != null && client.player.isOnGround();
	}

	private static boolean isHurt() {
		if (client.player != null) {
			return client.player.hurtTime > 0; // Do not use isHurt, that's not what we want!
		} else {
			return false;
		}
	}

	private static boolean isInvisible() {
		if (client.player != null) {
			return client.player.isInvisible();
		} else {
			return false;
		}
	}

	private static boolean isBurning() {
		if (client.player != null) {
			return client.player.isOnFire();
		} else {
			return false;
		}
	}

	private static boolean isSneaking() {
		if (client.player != null) {
			return client.player.isCrouching();
		} else {
			return false;
		}
	}

	private static boolean isSprinting() {
		if (client.player != null) {
			return client.player.isSprinting();
		} else {
			return false;
		}
	}

	private static Vector3d getSkyColor() {
		if (client.level == null || client.cameraEntity == null) {
			return ZERO_VECTOR_3d;
		}

		return JomlConversions.fromVec3(client.level.getSkyColor(client.cameraEntity.position(),
				CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			MobEffectInstance blindness = ((LivingEntity) cameraEntity).getEffect(MobEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in FogRenderer
				// TODO: Add this to ShaderDoc
				return Math.clamp(0.0F, 1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	static float getDarknessFactor() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			MobEffectInstance darkness = ((LivingEntity) cameraEntity).getEffect(MobEffects.DARKNESS);

			if (darkness != null && darkness.getFactorData().isPresent()) {
				return darkness.getFactorData().get().getFactor((LivingEntity) cameraEntity, CapturedRenderingState.INSTANCE.getTickDelta());
			}
		}

		return 0.0F;
	}

	private static float getPlayerMood() {
		if (!(client.cameraEntity instanceof LocalPlayer)) {
			return 0.0F;
		}

		// This should always be 0 to 1 anyways but just making sure
		return Math.clamp(0.0F, 1.0F, ((LocalPlayer) client.cameraEntity).getCurrentMood());
	}

	static float getRainStrength() {
		if (client.level == null) {
			return 0f;
		}

		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			client.level.getRainLevel(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static Vector2i getEyeBrightness() {
		if (client.cameraEntity == null || client.level == null) {
			return ZERO_VECTOR_2i;
		}

		Vec3 feet = client.cameraEntity.position();
		Vec3 eyes = new Vec3(feet.x, client.cameraEntity.getEyeY(), feet.z);
		BlockPos eyeBlockPos = new BlockPos(eyes);

		int blockLight = client.level.getBrightness(LightLayer.BLOCK, eyeBlockPos);
		int skyLight = client.level.getBrightness(LightLayer.SKY, eyeBlockPos);

		return new Vector2i(blockLight * 16, skyLight * 16);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) cameraEntity;

			try {
				// See MixinGameRenderer#iris$safecheckNightvisionStrength.
				//
				// We modify the behavior of getNightVisionScale so that it's safe for us to call it even on entities
				// that don't have the effect, allowing us to pick up modified night vision strength values from mods
				// like Origins.
				//
				// See: https://github.com/apace100/apoli/blob/320b0ef547fbbf703de7154f60909d30366f6500/src/main/java/io/github/apace100/apoli/mixin/GameRendererMixin.java#L153
				float nightVisionStrength =
						GameRenderer.getNightVisionScale(livingEntity, CapturedRenderingState.INSTANCE.getTickDelta());

				if (nightVisionStrength > 0) {
					// Just protecting against potential weird mod behavior
					return Math.clamp(0.0F, 1.0F, nightVisionStrength);
				}
			} catch (NullPointerException e) {
				// If our injection didn't get applied, a NullPointerException will occur from calling that method if
				// the entity doesn't currently have night vision. This isn't pretty but it's functional.
				return 0.0F;
			}
		}

		// Conduit power gives the player a sort-of night vision effect when underwater.
		// This lets existing shaderpacks be compatible with conduit power automatically.
		//
		// Yes, this should be the player entity, to match LightTexture.
		if (client.player != null && client.player.hasEffect(MobEffects.CONDUIT_POWER)) {
			float underwaterVisibility = client.player.getWaterVision();

			if (underwaterVisibility > 0.0f) {
				// Just protecting against potential weird mod behavior
				return Math.clamp(0.0F, 1.0F, underwaterVisibility);
			}
		}

		return 0.0F;
	}

	static int isEyeInWater() {
		// Note: With certain utility / cheat mods, this method will return air even when the player is submerged when
		// the "No Overlay" feature is enabled.
		//
		// I'm not sure what the best way to deal with this is, but the current approach seems to be an acceptable one -
		// after all, disabling the overlay results in the intended effect of it not really looking like you're
		// underwater on most shaderpacks. For now, I will leave this as-is, but it is something to keep in mind.
		FogType submersionType = client.gameRenderer.getMainCamera().getFluidInCamera();

		if (submersionType == FogType.WATER) {
			return 1;
		} else if (submersionType == FogType.LAVA) {
			return 2;
		} else if (submersionType == FogType.POWDER_SNOW) {
			return 3;
		} else {
			return 0;
		}
	}

	static {
		GbufferPrograms.init();
	}
}
