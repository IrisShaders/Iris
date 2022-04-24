package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.mixin.DimensionTypeAccessor;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.vendored.joml.Math;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

// These expressions are copied directly from BSL and Complementary.

// TODO: Remove once custom uniforms are actually supported, this is just a temporary thing to get BSL & Complementary
// mostly working under Iris.
public class HardcodedCustomUniforms {
	private static final Minecraft client = Minecraft.getInstance();

	public static void addHardcodedCustomUniforms(UniformHolder holder, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.CameraPositionTracker tracker = new CameraUniforms.CameraPositionTracker(updateNotifier);

		SmoothedFloat eyeInCave = new SmoothedFloat(6, 12, HardcodedCustomUniforms::getEyeInCave, updateNotifier);
		SmoothedFloat rainStrengthS = rainStrengthS(updateNotifier);

		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeAngle", HardcodedCustomUniforms::getTimeAngle);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeBrightness", HardcodedCustomUniforms::getTimeBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "moonBrightness", HardcodedCustomUniforms::getMoonBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "shadowFade", HardcodedCustomUniforms::getShadowFade);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainStrengthS", rainStrengthS);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "blindFactor", HardcodedCustomUniforms::getBlindFactor);
		// The following uniforms are Complementary specific, used for the biome check and starter/TAA features.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isDry", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 0 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isRainy", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 1 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isSnowy", new SmoothedFloat(20, 10, () -> getRawPrecipitation() == 2 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isEyeInCave", CommonUniforms.isEyeInWater() == 0 ? eyeInCave : () -> 0);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "velocity", () -> getVelocity(tracker));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "starter", getStarter(tracker, updateNotifier));
		// The following uniforms are Project Reimagined specific.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTimeSmooth", new SmoothedFloat(5, 5, SystemTimeUniforms.TIMER::getLastFrameTime, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "eyeBrightnessM", new SmoothedFloat(5, 5, HardcodedCustomUniforms::getEyeBrightnessM, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainFactor", rainStrengthS);
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
		BlockPos eyeBlockPos = new BlockPos(eyes);

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
		long dayTime = ((DimensionTypeAccessor) level.dimensionType()).getFixedTime().orElse(timeOfDay % 24000L);

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

	private static SmoothedFloat rainStrengthS(FrameUpdateNotifier updateNotifier) {
		return new SmoothedFloat(15, 15, CommonUniforms::getRainStrength, updateNotifier);
	}

	private static float getRawPrecipitation() {
		if (Minecraft.getInstance().level == null) {
			return 0;
		}
		Biome.Precipitation precipitation = Minecraft.getInstance().level.getBiome(Minecraft.getInstance().getCameraEntity().blockPosition()).value().getPrecipitation();
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
}
