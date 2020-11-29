package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.Objects;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#celestial-bodies">Uniforms: Celestial bodies</a>
 */
public final class CelestialUniforms {
	private CelestialUniforms() {
	}

	public static void addCelestialUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_FRAME, "sunAngle", CelestialUniforms::getSunAngle)
			.uniformTruncated3f(PER_FRAME, "sunPosition", CelestialUniforms::getSunPosition)
			.uniformTruncated3f(PER_FRAME, "moonPosition", CelestialUniforms::getMoonPosition)
			.uniform1f(PER_FRAME, "shadowAngle", CelestialUniforms::getShadowAngle)
			.uniformTruncated3f(PER_FRAME, "shadowLightPosition", CelestialUniforms::getShadowLightPosition)
			.uniformTruncated3f(PER_FRAME, "upPosition", CelestialUniforms::getUpPosition);
	}

	private static float getSunAngle() {
		float skyAngle = getSkyAngle();

		if (skyAngle < 0.75F) {
			return skyAngle + 0.25F;
		} else {
			return skyAngle - 0.75F;
		}
	}

	private static float getShadowAngle() {
		float shadowAngle = getSunAngle();

		if (!isDay()) {
			shadowAngle -= 0.5F;
		}

		return shadowAngle;
	}

	private static Vector4f getSunPosition() {
		return getCelestialPosition(100.0F);
	}

	private static Vector4f getMoonPosition() {
		return getCelestialPosition(-100.0F);
	}

	private static Vector4f getShadowLightPosition() {
		return isDay() ? getSunPosition() : getMoonPosition();
	}

	private static Vector4f getCelestialPosition(float y) {
		Vector4f position = new Vector4f(0.0F, y, 0.0F, 0.0F);

		Matrix4f celestial = CapturedRenderingState.INSTANCE.getGbufferModelView().copy();

		// This is the same transformation applied by renderSky, however, it's been moved to here.
		// This is because we need the result of it before it's actually performed in vanilla.
		celestial.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
		celestial.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(getSkyAngle() * 360.0F));

		position.transform(celestial);

		return position;
	}

	private static Vector4f getUpPosition() {
		Vector4f upVector = new Vector4f(0.0F, 100.0F, 0.0F, 0.0F);

		// Get the current GBuffer model view matrix, since that is the basis of the celestial model view matrix
		Matrix4f preCelestial = CapturedRenderingState.INSTANCE.getGbufferModelView().copy();

		// Apply the fixed -90.0F degrees rotation to mirror the same transformation in renderSky.
		// But, notably, skip the rotation by the skyAngle.
		preCelestial.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));

		// Use this matrix to transform the vector.
		upVector.transform(preCelestial);

		return upVector;
	}

	private static boolean isDay() {
		return getWorld().isDay();
	}

	private static ClientWorld getWorld() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world);
	}

	private static float getSkyAngle() {
		return getWorld().getSkyAngle(CapturedRenderingState.INSTANCE.getTickDelta());
	}
}
