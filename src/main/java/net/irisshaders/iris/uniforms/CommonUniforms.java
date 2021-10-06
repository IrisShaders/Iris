package net.irisshaders.iris.uniforms;

import java.util.Objects;
import java.util.function.IntSupplier;

import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.layer.EntityColorRenderStateShard;
import net.irisshaders.iris.mixin.statelisteners.GlStateManagerAccessor;
import net.irisshaders.iris.samplers.TextureAtlasTracker;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.PackDirectives;
import net.irisshaders.iris.uniforms.transforms.SmoothedFloat;
import net.irisshaders.iris.uniforms.transforms.SmoothedVec2f;
import net.irisshaders.iris.vendored.joml.Vector2i;
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

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import com.mojang.math.Vector4f;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.ONCE;

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

		uniforms.uniform4f("entityColor", () -> {
			if (EntityColorRenderStateShard.currentHurt) {
				return new Vector4f(1.0f, 0.0f, 0.0f, 0.3f);
			}

			float shade = EntityColorRenderStateShard.currentWhiteFlash;

			if (shade != 0.0f) {
				return new Vector4f(shade, shade, shade, 0.5f);
			}

			return new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
		}, EntityColorRenderStateShard.getUpdateNotifier());

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		uniforms.uniform2i("atlasSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].binding;

			Vec2 atlasSize = TextureAtlasTracker.INSTANCE.getAtlasSize(glId);

			return new Vector2i((int) atlasSize.x, (int) atlasSize.y);
		}, StateUpdateNotifiers.atlasTextureNotifier);

		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier){
		ExternallyManagedUniforms.addExternallyManagedUniforms116(uniforms);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hideGui)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getEyeY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1i(PER_FRAME, "heldBlockLightValue", new HeldItemLightingSupplier(InteractionHand.MAIN_HAND))
			.uniform1i(PER_FRAME, "heldBlockLightValue2", new HeldItemLightingSupplier(InteractionHand.OFF_HAND))
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			// TODO: Parse the value of const float eyeBrightnessHalflife from the shaderpack's fragment shader configuration
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", new SmoothedVec2f(10.0f, CommonUniforms::getEyeBrightness, updateNotifier))
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			// TODO: Parse the value of const float wetnessHalfLife and const float drynessHalfLife from the shaderpack's fragment shader configuration
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(600f, CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor)
			.uniform3d(PER_FRAME, "fogColor", CapturedRenderingState.INSTANCE::getFogColor);
	}

	private static Vec3 getSkyColor() {
		if (client.level == null || client.cameraEntity == null) {
			return Vec3.ZERO;
		}

		return client.level.getSkyColor(client.cameraEntity.blockPosition(), CapturedRenderingState.INSTANCE.getTickDelta());
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

	private static Vec2 getEyeBrightness() {
		if (client.cameraEntity == null || client.level == null) {
			return Vec2.ZERO;
		}

		Vec3 feet = client.cameraEntity.position();
		Vec3 eyes = new Vec3(feet.x, client.cameraEntity.getEyeY(), feet.z);
		BlockPos eyeBlockPos = new BlockPos(eyes);

		int blockLight = client.level.getBrightness(LightLayer.BLOCK, eyeBlockPos);
		int skyLight = client.level.getBrightness(LightLayer.SKY, eyeBlockPos);

		return new Vec2(blockLight * 16.0f, skyLight * 16.0f);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) cameraEntity;

			if (livingEntity.getEffect(MobEffects.NIGHT_VISION) != null) {
				return GameRenderer.getNightVisionScale(livingEntity, CapturedRenderingState.INSTANCE.getTickDelta());
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

	private static int isEyeInWater() {
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

}
