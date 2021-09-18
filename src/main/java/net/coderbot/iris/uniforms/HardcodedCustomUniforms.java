package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.mixin.DimensionTypeAccessor;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.vendored.joml.Math;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

// These expressions are copied directly from BSL and Complementary.
//
// TODO: Remove once custom uniforms are actually supported, this is just a temporary thing to get BSL & Complementary
// mostly working under Iris.
public class HardcodedCustomUniforms {
	private static Minecraft client = Minecraft.getInstance();

	public static void addHardcodedCustomUniforms(UniformHolder holder, FrameUpdateNotifier updateNotifier) {
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeAngle", HardcodedCustomUniforms::getTimeAngle);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeBrightness", HardcodedCustomUniforms::getTimeBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "moonBrightness", HardcodedCustomUniforms::getMoonBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "shadowFade", HardcodedCustomUniforms::getShadowFade);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "rainStrengthS", rainStrengthS(updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "blindFactor", HardcodedCustomUniforms::getBlindFactor);
		// The following uniforms are Complementary specific.
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isEyeInCave", isEyeInCave(updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isDry", precipitationComparison(updateNotifier, 0));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isRainy", precipitationComparison(updateNotifier, 1));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isSnowy", precipitationComparison(updateNotifier, 2));
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
		return new SmoothedFloat(15, CommonUniforms::getRainStrength, updateNotifier);
	}

	private static SmoothedFloat isEyeInCave(FrameUpdateNotifier updateNotifier) {
		return new SmoothedFloat(6, HardcodedCustomUniforms::getCaveStatus, updateNotifier);
	}

	private static float getCaveStatus() {
		if (client.player.getEyeY() < 50.0) {
			return 1.0F - (CommonUniforms.getEyeBrightness().y / 240F);
		} else {
			return 0.0F;
		}
	}

	private static SmoothedFloat precipitationComparison(FrameUpdateNotifier updateNotifier, float expectedPrecipitation) {
		float comparison;
		if (getRawPrecipitation() == expectedPrecipitation) {
			comparison = 1;
		} else {
			comparison = 0;
		}
		return new SmoothedFloat(20, () -> comparison, updateNotifier);
	}

	private static float getRawPrecipitation() {
		Biome.Precipitation precipitation = client.level.getBiome(client.player.blockPosition()).getPrecipitation();
		switch (precipitation.getName()) {
			case "rain":
				return 1;
			case "snow":
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
