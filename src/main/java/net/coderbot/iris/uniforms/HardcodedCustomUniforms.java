package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.mixin.DimensionTypeAccessor;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.joml.Math;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

// These expressions are copied directly from BSL and Complementary.

// TODO: Remove once custom uniforms are actually supported, this is just a temporary thing to get BSL & Complementary
// mostly working under Iris.
public class HardcodedCustomUniforms {
	private static final Minecraft client = Minecraft.getInstance();
	private static Holder<Biome> storedBiome;

	public static void addHardcodedCustomUniforms(UniformHolder holder, FrameUpdateNotifier updateNotifier) {
		updateNotifier.addListener(() -> {
			if (Minecraft.getInstance().level != null) {
				storedBiome = Minecraft.getInstance().level.getBiome(Minecraft.getInstance().getCameraEntity().blockPosition());
			} else {
				storedBiome = null;
			}
		});

		CameraUniforms.CameraPositionTracker tracker = new CameraUniforms.CameraPositionTracker(updateNotifier);

		SmoothedFloat eyeInCave = new SmoothedFloat(6, 12, HardcodedCustomUniforms::getEyeInCave, updateNotifier);
		SmoothedFloat rainStrengthS = rainStrengthS(updateNotifier, 15, 15);
		SmoothedFloat rainStrengthShining = rainStrengthS(updateNotifier, 10, 11);
		SmoothedFloat rainStrengthS2 = rainStrengthS(updateNotifier, 70, 1);

		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeAngle", HardcodedCustomUniforms::getTimeAngle);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeBrightness", HardcodedCustomUniforms::getTimeBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "moonBrightness", HardcodedCustomUniforms::getMoonBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "shadowFade", HardcodedCustomUniforms::getShadowFade);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainStrengthS", rainStrengthS);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainStrengthShiningStars", rainStrengthShining);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainStrengthS2", rainStrengthS2);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "blindFactor", HardcodedCustomUniforms::getBlindFactor);
		// The following uniforms are Complementary specific, used for the biome check and starter/TAA features.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isDry", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 0 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isRainy", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 1 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isSnowy", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 2 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isEyeInCave", () -> CommonUniforms.isEyeInWater() == 0 ? eyeInCave.getAsFloat() : 0);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "velocity", () -> getVelocity(tracker));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "starter", getStarter(tracker, updateNotifier));
		// The following uniforms are Project Reimagined specific.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTimeSmooth", new SmoothedFloat(5, 5, SystemTimeUniforms.TIMER::getLastFrameTime, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "eyeBrightnessM", new SmoothedFloat(5, 5, HardcodedCustomUniforms::getEyeBrightnessM, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainFactor", rainStrengthS);

		// The following uniforms are Sildur's specific.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "inSwamp", new SmoothedFloat(5, 5, () -> {
			if (storedBiome == null) {
				return 0;
			} else {
				// Easiest way to check for "swamp" on 1.19.
				return storedBiome.is(BiomeTags.HAS_CLOSER_WATER_FOG) ? 1 : 0;
			}
		}, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "BiomeTemp", () -> {
			if (storedBiome == null) {
				return 0;
			} else {
				return storedBiome.value().getBaseTemperature();
			}
		});

		// The following uniforms are specific to Super Duper Vanilla Shaders.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "day", HardcodedCustomUniforms::getDay);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "night", HardcodedCustomUniforms::getNight);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "dawnDusk", HardcodedCustomUniforms::getDawnDusk);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "shdFade", HardcodedCustomUniforms::getShdFade);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isPrecipitationRain", new SmoothedFloat(6, 6, () -> (getRawPrecipitation() == 1 && tracker.getCurrentCameraPosition().y < 96.0f) ? 1 : 0, updateNotifier));

		// The following uniforms are specific to AstralEX, and require an active player.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "touchmybody", new SmoothedFloat(0f, 0.1f, HardcodedCustomUniforms::getHurtFactor, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "sneakSmooth", new SmoothedFloat(2.0f, 0.9f, HardcodedCustomUniforms::getSneakFactor, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "burningSmooth", new SmoothedFloat(1.0f, 2.0f, HardcodedCustomUniforms::getBurnFactor, updateNotifier));
		SmoothedFloat smoothSpeed = new SmoothedFloat(1.0f, 1.5f, () -> getVelocity(tracker) / SystemTimeUniforms.TIMER.getLastFrameTime(), updateNotifier);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "effectStrength", () -> getHyperSpeedStrength(smoothSpeed));
	}

	private static float getHyperSpeedStrength(SmoothedFloat smoothSpeed) {
		return (float) (1.0f - Math.exp(-smoothSpeed.getAsFloat() * 0.003906f));
	}

	private static float getBurnFactor() {
		return Minecraft.getInstance().player.isOnFire() ? 1.0f : 0f;
	}

	private static float getSneakFactor() {
		return Minecraft.getInstance().player.isCrouching() ? 1.0f : 0f;
	}

	private static float getHurtFactor() {
		Player player = Minecraft.getInstance().player;
		return player.hurtTime > 0 || player.deathTime > 0 ? 0.4f : 0f;
	}

	private static float getEyeInCave() {
		if (client.getCameraEntity().getEyeY() < 5.0) {
			return 1.0f - getEyeSkyBrightness() / 240F;
		}
		return 0.0f;
	}

	private static float getEyeBrightnessM() {
		return getEyeSkyBrightness() / 240F;
	}

	private static float getEyeSkyBrightness() {
		if (client.cameraEntity == null || client.level == null) {
			return 0;
		}

		Vec3 feet = client.cameraEntity.position();
		Vec3 eyes = new Vec3(feet.x, client.cameraEntity.getEyeY(), feet.z);
		BlockPos eyeBlockPos = BlockPos.containing(eyes);

		int skyLight = client.level.getBrightness(LightLayer.SKY, eyeBlockPos);

		return skyLight * 16;
	}

	private static float getVelocity(CameraUniforms.CameraPositionTracker tracker) {
		float difX = (float) (tracker.getCurrentCameraPosition().x - tracker.getPreviousCameraPosition().x);
		float difY = (float) (tracker.getCurrentCameraPosition().y - tracker.getPreviousCameraPosition().y);
		float difZ = (float) (tracker.getCurrentCameraPosition().z - tracker.getPreviousCameraPosition().z);
		return Math.sqrt(difX*difX + difY*difY + difZ*difZ);
	}

	private static SmoothedFloat getStarter(CameraUniforms.CameraPositionTracker tracker, FrameUpdateNotifier notifier) {
		return new SmoothedFloat(20, 20, new SmoothedFloat(0, 31536000, () -> getMoving(tracker), notifier), notifier);
	}

	private static float getMoving(CameraUniforms.CameraPositionTracker tracker) {
		float difX = (float) (tracker.getCurrentCameraPosition().x - tracker.getPreviousCameraPosition().x);
		float difY = (float) (tracker.getCurrentCameraPosition().y - tracker.getPreviousCameraPosition().y);
		float difZ = (float) (tracker.getCurrentCameraPosition().z - tracker.getPreviousCameraPosition().z);
		float difSum = Math.abs(difX) + Math.abs(difY) + Math.abs(difZ);
		return (difSum > 0.0F && difSum < 1.0F) ? 1 : 0;
	}

	private static float getTimeAngle() {
		return getWorldDayTime() / 24000F;
	}

	private static int getWorldDayTime() {
		Level level = Minecraft.getInstance().level;
		long  timeOfDay = level.getDayTime();
		long dayTime = level.dimensionType().fixedTime().orElse(timeOfDay % 24000L);

		return (int) dayTime;
	}

	private static float getTimeBrightness() {
		return (float) java.lang.Math.max(java.lang.Math.sin(getTimeAngle() * java.lang.Math.PI * 2.0),0.0);
	}

	private static float getMoonBrightness() {
		return (float) java.lang.Math.max(java.lang.Math.sin(getTimeAngle() * java.lang.Math.PI * (-2.0)),0.0);
	}

	private static float getShadowFade() {
		return (float) Math.clamp(0.0, 1.0, 1.0 - (java.lang.Math.abs(java.lang.Math.abs(CelestialUniforms.getSunAngle() - 0.5) - 0.25) - 0.23) * 100.0);
	}

	private static SmoothedFloat rainStrengthS(FrameUpdateNotifier updateNotifier, float halfLifeUp, float halfLifeDown) {
		return new SmoothedFloat(halfLifeUp, halfLifeDown, CommonUniforms::getRainStrength, updateNotifier);
	}

	private static float getRawPrecipitation() {
		if (storedBiome == null) {
			return 0;
		}
		Biome.Precipitation precipitation = storedBiome.value().getPrecipitationAt(Minecraft.getInstance().cameraEntity.blockPosition());
		switch (precipitation) {
			case RAIN:
				return 1;
			case SNOW:
				return 2;
			default:
				return 0;
		}
	}

	private static float getBlindFactor() {
		float blindFactorSqrt = (float) Math.clamp(0.0, 1.0, CommonUniforms.getBlindness() * 2.0 - 1.0);
		return blindFactorSqrt * blindFactorSqrt;
	}

	private static float frac(float value) {
		return java.lang.Math.abs(value % 1);
	}

	private static float getAdjTime() {
		return Math.abs(((((WorldTimeUniforms.getWorldDayTime()) / 1000.0f) + 6.0f) % 24.0f) - 12.0f);
	}

	private static float getDay() {
		return Math.clamp(0.0f, 1.0f, 5.4f - getAdjTime());
	}

	private static float getNight() {
		return Math.clamp(0.0f, 1.0f, getAdjTime() - 6.0f);
	}

	private static float getDawnDusk() {
		return (1.0f - getDay()) - getNight();
	}


	private static float getShdFade() {
		return (float) Math.clamp(0.0, 1.0, 1.0 - (Math.abs(Math.abs(CelestialUniforms.getSunAngle() - 0.5) - 0.25) - 0.225) * 40.0);
	}
}
