package net.irisshaders.iris.parsing;

import net.irisshaders.iris.uniforms.SystemTimeUniforms;

/**
 * An implementation of basic exponential smoothing that converts a sequence of unsmoothed values into a sequence of
 * smoothed values.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Exponential_smoothing#Basic_(simple)_exponential_smoothing_(Holt_linear)">
 * Wikipedia: Basic (simple) exponential smoothing (Holt linear)</a>
 */
public class SmoothFloat {
	/**
	 * Natural logarithm of 2, ie. {@code ln(2)}
	 */
	private static final double LN_OF_2 = Math.log(2.0);

	/**
	 * An accumulator for smoothed values.
	 */
	private float accumulator;

	/**
	 * Tracks whether an initial value has already been generated, because otherwise there will be nothing to smooth
	 * with.
	 */
	private boolean hasInitialValue;
	private float cachedHalfLifeUp;
	private float cachedDecayUp;
	private float cachedHalfLifeDown;
	private float cachedDecayDown;

	/**
	 * Computes an exponential decay factor based on the given decay constant and time value
	 *
	 * @param k the decay constant, derived from the half life
	 * @param t the time that has passed since the decay started
	 */
	private static float exponentialDecayFactor(float k, float t) {
		// https://en.wikipedia.org/wiki/Exponential_decay
		// e^(-kt)
		return (float) Math.exp(-k * t);
	}

	/**
	 * Computes a linearly interpolated value between v0 and v1
	 *
	 * @param v0 the starting value (t = 0)
	 * @param v1 the ending value (t = 1)
	 * @param t  the time/progress value - should be in the range of 0.0 to 1.0
	 */
	private static float lerp(float v0, float v1, float t) {
		// https://en.wikipedia.org/wiki/Linear_interpolation
		return (1 - t) * v0 + t * v1;
	}

	/**
	 * Takes one value from the unsmoothed value sequence, and smooths it into our accumulator
	 */
	public float updateAndGet(float value, float halfLifeUp, float halfLifeDown) {
		if (halfLifeUp != cachedHalfLifeUp) {
			cachedHalfLifeUp = halfLifeUp;
			if (halfLifeUp == 0.0f) {
				cachedDecayUp = 0;
			} else {
				cachedDecayUp = computeDecay(halfLifeUp * 0.1F);
			}
		}

		if (halfLifeDown != cachedHalfLifeDown) {
			cachedHalfLifeDown = halfLifeDown;
			if (halfLifeDown == 0.0f) {
				cachedDecayDown = 0;
			} else {
				cachedDecayDown = computeDecay(halfLifeDown * 0.1F);
			}
		}

		if (!hasInitialValue) {
			// There is no smoothing on the first value.
			// This is not an optimal approach to choosing the initial value:
			// https://en.wikipedia.org/wiki/Exponential_smoothing#Choosing_the_initial_smoothed_value
			//
			// However, it works well enough for now.
			accumulator = value;
			hasInitialValue = true;

			return accumulator;
		}

		// Implements the basic variant of exponential smoothing
		// https://en.wikipedia.org/wiki/Exponential_smoothing#Basic_(simple)_exponential_smoothing_(Holt_linear)

		// 𝚫t
		float lastFrameTime = SystemTimeUniforms.TIMER.getLastFrameTime();

		float decay = value > this.accumulator ? cachedDecayUp : cachedDecayDown;

		if (decay == 0.0f) {
			accumulator = value;
			return accumulator;
		}

		// Compute the smoothing factor based on our
		// α = 1 - e^(-𝚫t/τ) = 1 - e^(-k𝚫t)
		float smoothingFactor = 1.0f - exponentialDecayFactor(decay, lastFrameTime);

		// sₜ = αxₜ + (1 - α)sₜ₋₁
		accumulator = lerp(accumulator, value, smoothingFactor);

		return accumulator;
	}

	private float computeDecay(float halfLife) {
		// Compute the decay constant from the half life
		// https://en.wikipedia.org/wiki/Exponential_decay#Measuring_rates_of_decay
		// https://en.wikipedia.org/wiki/Exponential_smoothing#Time_constant
		// k = 1 / τ
		return (float) (1.0f / (halfLife / LN_OF_2));
	}
}
