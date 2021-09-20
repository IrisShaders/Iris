package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.FloatSupplier;
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
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isDry", new SmoothedFloat(20, () -> getRawPrecipitation() == 0 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isRainy", new SmoothedFloat(20, () -> getRawPrecipitation() == 1 ? 1 : 0, updateNotifier));
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "isSnowy", new SmoothedFloat(20, () -> getRawPrecipitation() == 2 ? 1 : 0, updateNotifier));
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

	private static FloatSupplier isEyeInCave(FrameUpdateNotifier updateNotifier) {
		return new EyeInCaveSmoothedFloat(6, HardcodedCustomUniforms::getCaveStatus, updateNotifier);
	}

	private static float getCaveStatus() {
		if (client.player.getEyeY() < 50.0) {
			return 1.0F - (CommonUniforms.getEyeBrightness().y / 240F);
		} else {
			return 0.0F;
		}
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

	private static class EyeInCaveSmoothedFloat extends SmoothedFloat implements FloatSupplier {

		/**
		 * Creates a smoothed float for the isEyeInCave uniform, to change to 0.0 when in water.
		 *
		 * @param halfLife       the half life in the exponential decay, in deciseconds (1/10th of a second) / 2 ticks.
		 *                       For example, a half life of value of 2.0 is 4 ticks or 0.2 seconds
		 * @param unsmoothed     the input sequence of unsmoothed values to be smoothed. {@code unsmoothed.getAsFloat()} will be
		 *                       called exactly once for every time {@code smoothed.getAsFloat()} is called.
		 * @param updateNotifier
		 */
		public EyeInCaveSmoothedFloat(float halfLife, FloatSupplier unsmoothed, FrameUpdateNotifier updateNotifier) {
			super(halfLife, unsmoothed, updateNotifier);
		}

		@Override
		protected void update() {
			if (!hasInitialValue) {
				// There is no smoothing on the first value.
				// This is not an optimal approach to choosing the initial value:
				// https://en.wikipedia.org/wiki/Exponential_smoothing#Choosing_the_initial_smoothed_value
				//
				// However, it works well enough for now.
				accumulator = unsmoothed.getAsFloat();
				hasInitialValue = true;

				return;
			}

			// Implements the basic variant of exponential smoothing
			// https://en.wikipedia.org/wiki/Exponential_smoothing#Basic_(simple)_exponential_smoothing_(Holt_linear)

			// xₜ
			float newValue = unsmoothed.getAsFloat();

			// 𝚫t
			float lastFrameTime = SystemTimeUniforms.TIMER.getLastFrameTime();

			// Compute the smoothing factor based on our
			// α = 1 - e^(-𝚫t/τ) = 1 - e^(-k𝚫t)
			float smoothingFactor = 1.0f - exponentialDecayFactor(this.decayConstant, lastFrameTime);

			// sₜ = αxₜ + (1 - α)sₜ₋₁
			if (CommonUniforms.isEyeInWater() != 0) {
				accumulator = 0.0F;
			} else {
				accumulator = lerp(accumulator, newValue, smoothingFactor);
			}
		}
	}
}
