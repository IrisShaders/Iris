package net.coderbot.iris.uniforms;

import java.util.Objects;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.uniforms.transforms.SmoothedVec2f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class CommonUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addCommonUniforms(LocationalUniformHolder uniforms, IdMap idMap, PackDirectives directives) {
		CameraUniforms.addCameraUniforms(uniforms);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IdMapUniforms.addIdMapUniforms(uniforms, idMap);
		MatrixUniforms.addMatrixUniforms(uniforms);
		SamplerUniforms.addCommonSamplerUniforms(uniforms);
		HardcodedCustomUniforms.addHardcodedCustomUniforms(uniforms);

		CommonUniforms.generalCommonUniforms(uniforms);
	}

	public static void generalCommonUniforms(UniformHolder uniforms){
		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getEyeY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1i(PER_FRAME, "heldBlockLightValue", new HeldItemLightingSupplier(Hand.MAIN_HAND))
			.uniform1i(PER_FRAME, "heldBlockLightValue2", new HeldItemLightingSupplier(Hand.OFF_HAND))
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", new SmoothedVec2f(10.0f, CommonUniforms::getEyeBrightness))
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(600f, CommonUniforms::getRainStrength))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor);
	}

	private static Vec3d getSkyColor() {
		if (client.world == null || client.cameraEntity == null) {
			return Vec3d.ZERO;
		}

		return client.world.method_23777(client.cameraEntity.getBlockPos(), CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance blindness = ((LivingEntity) cameraEntity).getStatusEffect(StatusEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in BackgroundRenderer
				// TODO: Add this to ShaderDoc
				return Math.min(1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	private static float getPlayerMood() {
		if (!(client.cameraEntity instanceof ClientPlayerEntity)) {
			return 0.0F;
		}

		return ((ClientPlayerEntity)client.cameraEntity).getMoodPercentage();
	}

	private static float getRainStrength() {
		if (client.world == null) {
			return 0f;
		}

		return client.world.getRainGradient(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static Vec2f getEyeBrightness() {
		if (client.cameraEntity == null || client.world == null) {
			return Vec2f.ZERO;
		}

		Vec3d feet = client.cameraEntity.getPos();
		Vec3d eyes = new Vec3d(feet.x, client.cameraEntity.getEyeY(), feet.z);
		BlockPos eyeBlockPos = new BlockPos(eyes);

		int blockLight = client.world.getLightLevel(LightType.BLOCK, eyeBlockPos);
		int skyLight = client.world.getLightLevel(LightType.SKY, eyeBlockPos);

		return new Vec2f(blockLight * 16.0f, skyLight * 16.0f);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) cameraEntity;

			if (livingEntity.getStatusEffect(StatusEffects.NIGHT_VISION) != null) {
				return GameRenderer.getNightVisionStrength(livingEntity, CapturedRenderingState.INSTANCE.getTickDelta());
			}
		}

		// Conduit power gives the player a sort-of night vision effect when underwater.
		// This lets existing shaderpacks be compatible with conduit power automatically.
		//
		// Yes, this should be the player entity, to match LightmapTextureManager.
		if (client.player != null && client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
			float underwaterVisibility = client.player.getUnderwaterVisibility();

			if (underwaterVisibility > 0.0f) {
				return underwaterVisibility;
			}
		}

		return 0.0F;
	}

	private static int isEyeInWater() {
		// Note: With certain utility / cheat mods, this method will return air even when the player is submerged when
		// the "No Overlay" feature is enabled.
		//
		// I'm not sure what the best way to deal with this is, but the current approach seems to be an acceptable one -
		// after all, disabling the overlay results in the intended effect of it not really looking like you're
		// underwater on most shaderpacks. For now, I will leave this as-is, but it is something to keep in mind.
		FluidState submergedFluid = client.gameRenderer.getCamera().getSubmergedFluidState();

		if (submergedFluid.isIn(FluidTags.WATER)) {
			return 1;
		} else if (submergedFluid.isIn(FluidTags.LAVA)) {
			return 2;
		} else {
			return 0;
		}
	}

	private static class HeldItemLightingSupplier implements IntSupplier {

		private final Hand hand;

		private HeldItemLightingSupplier(Hand targetHand) {
			this.hand = targetHand;
		}

		@Override
		public int getAsInt() {
			if (client.player == null) {
				return 0;
			}

			ItemStack stack = client.player.getStackInHand(hand);

			if (stack == ItemStack.EMPTY || stack == null || !(stack.getItem() instanceof BlockItem)) {
				return 0;
			}

			BlockItem item = (BlockItem) stack.getItem();

			return item.getBlock().getDefaultState().getLuminance();
		}
	}

}
