package net.coderbot.iris.uniforms;

import java.util.Objects;
import java.util.function.IntSupplier;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;
import net.coderbot.iris.mixin.statelisteners.GlStateManagerAccessor;
import net.coderbot.iris.samplers.TextureAtlasTracker;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.uniforms.transforms.SmoothedVec2f;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class CommonUniforms {
	private static final Minecraft client = Minecraft.getInstance();

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addCommonUniforms(DynamicUniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.addCameraUniforms(uniforms, updateNotifier);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IdMapUniforms.addIdMapUniforms(uniforms, idMap);
		IrisExclusiveUniforms.addIrisExclusiveUniforms(uniforms);
		MatrixUniforms.addMatrixUniforms(uniforms, directives);
		HardcodedCustomUniforms.addHardcodedCustomUniforms(uniforms, updateNotifier);
		FogUniforms.addFogUniforms(uniforms);

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		uniforms.uniform2i("atlasSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].binding;

			Vec2 atlasSize = TextureAtlasTracker.INSTANCE.getAtlasSize(glId);

			return new Vector2i((int) atlasSize.x, (int) atlasSize.y);
		}, StateUpdateNotifiers.atlasTextureNotifier);

		uniforms.uniform4i("blendFunc", () -> {
			GlStateManager.BlendState blend = net.coderbot.iris.mixin.GlStateManagerAccessor.getBLEND();

			if (((BooleanStateAccessor) blend.mode).isEnabled()) {
				return new Vector4i(blend.srcRgb, blend.dstRgb, blend.srcAlpha, blend.dstAlpha);
			} else {
				return new Vector4i(0, 0, 0, 0);
			}
		}, StateUpdateNotifiers.blendFuncNotifier);

		uniforms.uniform1i("renderStage", () -> GbufferPrograms.getCurrentPhase().ordinal(), StateUpdateNotifiers.phaseChangeNotifier);

		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier){
		ExternallyManagedUniforms.addExternallyManagedUniforms116(uniforms);

		// TODO: Parse the value of const float eyeBrightnessHalflife from the shaderpack's fragment shader configuration
		SmoothedVec2f eyeBrightnessSmooth = new SmoothedVec2f(10.0f, 10.0f, CommonUniforms::getEyeBrightness, updateNotifier);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hideGui)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getEyeY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1i(PER_FRAME, "heldBlockLightValue", new HeldItemLightingSupplier(InteractionHand.MAIN_HAND))
			.uniform1i(PER_FRAME, "heldBlockLightValue2", new HeldItemLightingSupplier(InteractionHand.OFF_HAND))
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma)
			// just a dummy value for shaders where entityColor isn't supplied through a vertex attribute (and thus is
			// not available) - suppresses warnings. See AttributeShaderTransformer for the actual entityColor code.
			.uniform4f(ONCE, "entityColor", Vector4f::new)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", () -> {
				Vector2f smoothed = eyeBrightnessSmooth.get();
				return new Vector2i((int) smoothed.x(),(int) smoothed.y());
			})
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			// TODO: Parse the value of const float wetnessHalfLife and const float drynessHalfLife from the shaderpack's fragment shader configuration
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(600f, 600f, CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor)
			.uniform3d(PER_FRAME, "fogColor", CapturedRenderingState.INSTANCE::getFogColor);
	}

	private static Vector3d getSkyColor() {
		if (client.level == null || client.cameraEntity == null) {
			return new Vector3d();
		}

		return JomlConversions.fromVec3(client.level.getSkyColor(client.cameraEntity.blockPosition(),
				CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			MobEffectInstance blindness = ((LivingEntity) cameraEntity).getEffect(MobEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in BackgroundRenderer
				// TODO: Add this to ShaderDoc
				return Math.min(1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	private static float getPlayerMood() {
		if (!(client.cameraEntity instanceof LocalPlayer)) {
			return 0.0F;
		}

		return ((LocalPlayer)client.cameraEntity).getCurrentMood();
	}

	static float getRainStrength() {
		if (client.level == null) {
			return 0f;
		}

		return client.level.getRainLevel(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static Vector2i getEyeBrightness() {
		if (client.cameraEntity == null || client.level == null) {
			return new Vector2i(0, 0);
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
					return nightVisionStrength;
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
		// Yes, this should be the player entity, to match LightmapTextureManager.
		if (client.player != null && client.player.hasEffect(MobEffects.CONDUIT_POWER)) {
			float underwaterVisibility = client.player.getWaterVision();

			if (underwaterVisibility > 0.0f) {
				return underwaterVisibility;
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
		FluidState submergedFluid = client.gameRenderer.getMainCamera().getFluidInCamera();

		if (submergedFluid.is(FluidTags.WATER)) {
			return 1;
		} else if (submergedFluid.is(FluidTags.LAVA)) {
			return 2;
		} else {
			return 0;
		}
	}

	private static class HeldItemLightingSupplier implements IntSupplier {

		private final InteractionHand hand;

		private HeldItemLightingSupplier(InteractionHand targetHand) {
			this.hand = targetHand;
		}

		@Override
		public int getAsInt() {
			if (client.player == null) {
				return 0;
			}

			ItemStack stack = client.player.getItemInHand(hand);

			if (stack == ItemStack.EMPTY || stack == null || !(stack.getItem() instanceof BlockItem)) {
				return 0;
			}

			BlockItem item = (BlockItem) stack.getItem();

			return item.getBlock().defaultBlockState().getLightEmission();
		}
	}

	static {
		GbufferPrograms.init();
	}
}
