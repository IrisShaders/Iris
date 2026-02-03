package net.irisshaders.iris.uniforms;

import com.mojang.math.Axis;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Objects;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#celestial-bodies">Uniforms: Celestial bodies</a>
 */
public final class CelestialUniforms {
	private static final Vector4f ZERO = new Vector4f();
	private final float sunPathRotation;

	public CelestialUniforms(float sunPathRotation) {
		this.sunPathRotation = sunPathRotation;
	}

	public static float getSunAngle(boolean sun) {
		float currentAngle = Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe().getValue(sun ? EnvironmentAttributes.SUN_ANGLE : EnvironmentAttributes.MOON_ANGLE, CapturedRenderingState.INSTANCE.getTickDelta());

		float c = currentAngle + 90.0f;

		if (c < 0) {
			c += 360;
		} else if (c > 360) {
			c -= 360;
		}

		return c;
	}

	private static float getShadowAngle() {
		float shadowAngle = getSunAngle(isDay());

		return shadowAngle / 360.0f;
	}

	private static Vector4f getUpPosition() {
		Vector4f upVector = new Vector4f(0.0F, 100.0F, 0.0F, 0.0F);

		// Get the current GBuffer model view matrix, since that is the basis of the celestial model view matrix
		Matrix4f preCelestial = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());

		// Apply the fixed -90.0F degrees rotation to mirror the same transformation in renderSky.
		// But, notably, skip the rotation by the skyAngle.
		preCelestial.rotate(Axis.YP.rotationDegrees(-90.0F));

		// Use this matrix to transform the vector.
		upVector = preCelestial.transform(upVector);

		return upVector;
	}

	public static boolean isDay() {
		// Determine whether it is day or night based on the sky angle.
		//
		// World#isDay appears to do some nontrivial calculations that appear to not entirely work for us here.
		int timeOfDay = Math.toIntExact((Minecraft.getInstance().level.getDayTime() % 24000));
		return timeOfDay < 12751 || timeOfDay > 23219; // TODO
	}

	private static ClientLevel getWorld() {
		return Objects.requireNonNull(Minecraft.getInstance().level);
	}


	public void addCelestialUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_FRAME, "sunAngle", () -> CelestialUniforms.getSunAngle(true) / 360.0f)
			.uniformTruncated3f(PER_FRAME, "sunPosition", this::getSunPosition)
			.uniformTruncated3f(PER_FRAME, "moonPosition", this::getMoonPosition)
			.uniform1f(PER_FRAME, "shadowAngle", CelestialUniforms::getShadowAngle)
			.uniformTruncated3f(PER_FRAME, "shadowLightPosition", this::getShadowLightPosition)
			.uniformTruncated3f(PER_FRAME, "endFlashPosition", () -> {
				if (Minecraft.getInstance().level.dimension() == Level.END) {
					return getEndFlashPosition();
				} else {
					return ZERO;
				}
			})
			.uniformTruncated3f(PER_FRAME, "upPosition", CelestialUniforms::getUpPosition);
	}

	private Vector4f getSunPosition() {
		return getCelestialPosition(true, 100.0F);
	}

	private Vector4f getMoonPosition() {
		return getCelestialPosition(false, -100.0F);
	}

	private Vector4f getEndFlashPosition() {
		EndFlashState state = Minecraft.getInstance().level.endFlashState();
		float h = state.getYAngle(); // yaw around Y
		float g = state.getXAngle(); // this feels silly

		Vector4f pos = new Vector4f(0f, 100f, 0f, 0f);

		Matrix4f m = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());
		m.rotate(Axis.YP.rotationDegrees(180.0F - h));
		m.rotate(Axis.XP.rotationDegrees(-90.0F - g));
		return m.transform(pos);
	}

	private Vector4f getEndFlashPositionInWorldSpace() {
		EndFlashState state = Minecraft.getInstance().level.endFlashState();
		float h = state.getYAngle();
		float g = state.getXAngle();

		Vector4f pos = new Vector4f(0f, 100f, 0f, 0f);
		Matrix4f m = new Matrix4f();
		m.identity();
		m.rotate(Axis.YP.rotationDegrees(180.0F - h));
		m.rotate(Axis.XP.rotationDegrees(-90.0F - g));
		return m.transform(pos);
	}

	public Vector4f getShadowLightPosition() {
		if (Minecraft.getInstance().level.dimension() == Level.END && Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::supportsEndFlash).orElse(false)) {
			return getEndFlashPosition();
		}
		return isDay() ? getSunPosition() : getMoonPosition();
	}

	public Vector4f getShadowLightPositionInWorldSpace() {
		if (Minecraft.getInstance().level.dimension() == Level.END && Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::supportsEndFlash).orElse(false)) {
			return getEndFlashPositionInWorldSpace();
		}
		return isDay() ? getCelestialPositionInWorldSpace(true, 100.0F)
			: getCelestialPositionInWorldSpace(false, -100.0F);
	}

	private Vector4f getCelestialPositionInWorldSpace(boolean sun, float y) {
		Vector4f position = new Vector4f(0.0F, 100.0f, 0.0F, 0.0F);

		// TODO: Deduplicate / remove this function.
		Matrix4f celestial = new Matrix4f();
		celestial.identity();

		// This is the same transformation applied by renderSky, however, it's been moved to here.
		// This is because we need the result of it before it's actually performed in vanilla.
		celestial.rotate(Axis.YP.rotationDegrees(-90.0F));
		celestial.rotate(Axis.ZP.rotationDegrees(sunPathRotation));
		float currentAngle = Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe().getValue(sun ? EnvironmentAttributes.SUN_ANGLE : EnvironmentAttributes.MOON_ANGLE, CapturedRenderingState.INSTANCE.getTickDelta());

		celestial.rotate(Axis.XP.rotationDegrees(currentAngle));
		celestial.transform(position);

		return position;
	}

	private Vector4f getCelestialPosition(boolean sun, float y) {
		Vector4f position = new Vector4f(0.0F, 100.0f, 0.0F, 1.0F);

		Matrix4f celestial = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());

		// This is the same transformation applied by renderSky, however, it's been moved to here.
		// This is because we need the result of it before it's actually performed in vanilla.
		celestial.rotate(Axis.YP.rotationDegrees(-90.0F));
		celestial.rotate(Axis.ZP.rotationDegrees(sunPathRotation));
		float currentAngle = Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe().getValue(sun ? EnvironmentAttributes.SUN_ANGLE : EnvironmentAttributes.MOON_ANGLE, CapturedRenderingState.INSTANCE.getTickDelta());

		celestial.rotate(Axis.XP.rotationDegrees(currentAngle));
		position = celestial.transform(position);

		return position;
	}
}
