package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.vendored.joml.Math;

// These expressions are copied directly from BSL
//
// TODO: Remove once custom uniforms are actually supported, this is just a temporary thing to get BSL & Complementary
// mostly working under Iris.
public class HardcodedCustomUniforms {
	public static void addHardcodedCustomUniforms(UniformHolder holder) {
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeAngle", HardcodedCustomUniforms::getTimeAngle);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "timeBrightness", HardcodedCustomUniforms::getTimeBrightness);
		holder.uniform1f(UniformUpdateFrequency.PER_FRAME, "shadowFade", HardcodedCustomUniforms::getShadowFade);
	}

	private static float getTimeAngle() {
		float sunAngle = CelestialUniforms.getSunAngle();
		float tAmin = frac(sunAngle - 0.033333333f);
		float tAlin = tAmin < 0.433333333f ? (tAmin * 1.15384615385f) : (tAmin * 0.882352941176f + 0.117647058824f);
		float hA = tAlin > 0.5f ? 1.0f : 0.0f;
		float tAfrc = frac(tAlin * 2.0f);
		float tAfrs = tAfrc*tAfrc*(3.0f-2.0f*tAfrc);
		float tAmix = hA < 0.5f ? 0.3f : -0.1f;

		return (tAfrc * (1.0f-tAmix) + tAfrs * tAmix + hA) * 0.5f;
	}

	private static float getTimeBrightness() {
		return (float) Math.max(Math.sin(getTimeAngle() * Math.PI * 2.0),0.0);
	}

	private static float getShadowFade() {
		return (float) Math.clamp(0.0, 1.0, 1.0 - (Math.abs(Math.abs(CelestialUniforms.getSunAngle() - 0.5) - 0.25) - 0.23) * 100.0);
	}

	private static float frac(float value) {
		return Math.abs(value % 1);
	}
}
