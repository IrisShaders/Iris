package net.irisshaders.iris.uniforms.transforms;

import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;

/**
 * An implementation of basic exponential smoothing that converts a sequence of unsmoothed values into a sequence of
 * smoothed values.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Exponential_smoothing#Basic_(simple)_exponential_smoothing_(Holt_linear)">
 * Wikipedia: Basic (simple) exponential smoothing (Holt linear)</a>
 */
public class SmoothedFloat implements FloatSupplier {
	/**
	 * Natural logarithm of 2, ie. {@code ln(2)}
	 */
	private static final double LN_OF_2 = Math.log(2.0);

	/**
	 * The input sequence of unsmoothed values
	 */
	private final FloatSupplier unsmoothed;
	/**
	 * The decay constant upward, k (as used in e^(-kt))
	 */
	private final float decayConstantUp;
	/**
	 * The decay constant downward, k (as used in e^(-kt))
	 */
	private final float decayConstantDown;
	/**
	 * An accumulator for smoothed values.
	 */
	private float accumulator;
	/**
	 * Tracks whether an initial value has already been generated, because otherwise there will be nothing to smooth
	 * with.
	 */
	private boolean hasInitialValue;

	/**
	 * Creates a new SmoothedFloat with a given half life.
	 *
	 * @param halfLifeUp the half life in the exponential decay, in deciseconds (1/10th of a second) / 2 ticks.
	 *                   For example, a half life of value of 2.0 is 4 ticks or 0.2 seconds
	 * @param unsmoothed the input sequence of unsmoothed values to be smoothed. {@code unsmoothed.getAsFloat()} will be
	 *                   called exactly once for every time {@code smoothed.getAsFloat()} is called.
	 */
	public SmoothedFloat(float halfLifeUp, float halfLifeDown, FloatSupplier unsmoothed, FrameUpdateNotifier updateNotifier) {
		// Half life is measured in units of 10ths of a second, or 2 ticks
		// For example, a half life of value of 2.0 is 4 ticks or 0.2 seconds
		this.decayConstantUp = computeDecay(halfLifeUp * 0.1F);
		this.decayConstantDown = computeDecay(halfLifeDown * 0.1F);

		this.unsmoothed = unsmoothed;

		updateNotifier.addListener(this::update);
	}

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
	private void update() {
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
		float smoothingFactor = 1.0f - exponentialDecayFactor(newValue > this.accumulator ? this.decayConstantUp : decayConstantDown, lastFrameTime);

		// sₜ = αxₜ + (1 - α)sₜ₋₁
		accumulator = lerp(accumulator, newValue, smoothingFactor);
	}

	private float computeDecay(float halfLife) {
		// Compute the decay constant from the half life
		// https://en.wikipedia.org/wiki/Exponential_decay#Measuring_rates_of_decay
		// https://en.wikipedia.org/wiki/Exponential_smoothing#Time_constant
		// k = 1 / τ
		return (float) (1.0f / (halfLife / LN_OF_2));
	}

	/**
	 * @return the current smoothed value
	 */
	@Override
	public float getAsFloat() {
		if (!hasInitialValue) {
			return unsmoothed.getAsFloat();
		}

		return accumulator;
	}
}
