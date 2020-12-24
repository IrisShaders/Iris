package net.coderbot.iris.uniforms;

import java.util.OptionalLong;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

/**
 * Implements uniforms relating the system time (as opposed to the world time)
 *
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#system-time">Uniforms: System time</a>
 */
public final class SystemTimeUniforms {
	public static final Timer TIMER = new Timer();

	private SystemTimeUniforms() {
	}

	/**
	 * Makes system time uniforms available to the given program
	 *
	 * @param uniforms the program to make the uniforms available to
	 */
	public static void addSystemTimeUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "frameCounter", new FrameCounter())
			.uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTime", TIMER::getLastFrameTime)
			.uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTimeCounter", TIMER::getFrameTimeCounter);
	}

	/**
	 * A simple frame counter. On each frame, it is incremented by 1, and it wraps around every 720720 frames. It starts
	 * at zero and goes from there.
	 */
	private static class FrameCounter implements IntSupplier {
		private int count;

		private FrameCounter() {
			this.count = 0;
		}

		@Override
		public int getAsInt() {
			int currentFrame = count;

			count = (count + 1) % 720720;

			return currentFrame;
		}
	}

	/**
	 * Keeps track of the time that the last frame took to render as well as the number of milliseconds since the start
	 * of the first frame to the start of the current frame. Updated at the start of each frame.
	 */
	public static final class Timer {
		private float frameTimeCounter;
		private float lastFrameTime;

		// Disabling this because OptionalLong provides a nice wrapper around (boolean valid, long value)
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private OptionalLong lastStartTime;

		public Timer() {
			frameTimeCounter = 0.0F;
			lastFrameTime = 0.0F;
			lastStartTime = OptionalLong.empty();
		}

		public void beginFrame(long frameStartTime) {
			// Track how much time passed since the last time we began rendering a frame.
			// If this is the first frame, then use a value of 0.
			long diffNs = frameStartTime - lastStartTime.orElse(frameStartTime);
			// Convert to milliseconds
			long diffMs = (diffNs / 1000) / 1000;

			// Convert to seconds with a resolution of 1 millisecond, and store as the time taken for the last frame to complete.
			lastFrameTime = diffMs / 1000.0F;

			// Advance the current frameTimeCounter by the amount of time the last frame took.
			frameTimeCounter += lastFrameTime;

			// Finally, update the "last start time" value.
			lastStartTime = OptionalLong.of(frameStartTime);
		}

		public float getFrameTimeCounter() {
			return frameTimeCounter;
		}

		public float getLastFrameTime() {
			return lastFrameTime;
		}
	}
}
