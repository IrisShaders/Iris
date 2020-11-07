package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class CommonUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CommonUniforms() {
		// no construction allowed
	}

	public static void addCommonUniforms(ProgramBuilder builder) {
		ViewportUniforms.addViewportUniforms(builder);
		WorldTimeUniforms.addWorldTimeUniforms(builder);
		SystemTimeUniforms.addSystemTimeUniforms(builder);
		CelestialUniforms.addCelestialUniforms(builder);

		builder
			.uniform1i(ONCE, "texture", TextureUnit.TERRAIN::getSamplerId)
			.uniform1i(ONCE, "lightmap", TextureUnit.LIGHTMAP::getSamplerId)
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniformMatrix(PER_FRAME, "gbufferModelView", CapturedRenderingState.INSTANCE::getGbufferModelView)
			.uniformMatrix(PER_FRAME, "gbufferModelViewInverse", CommonUniforms::getGbufferModelViewInverse)
			.uniformMatrix(PER_FRAME, "gbufferProjection", CapturedRenderingState.INSTANCE::getGbufferProjection)
			.uniformMatrix(PER_FRAME, "gbufferProjectionInverse", CommonUniforms::getGbufferProjectionInverse)
			.uniform3d(PER_FRAME, "cameraPosition", CommonUniforms::getCameraPosition);
	}

	private static Vec3d getCameraPosition() {
		return client.gameRenderer.getCamera().getPos();
	}

	private static Matrix4f getGbufferModelViewInverse() {
		return invertedCopy(CapturedRenderingState.INSTANCE.getGbufferModelView());
	}

	private static Matrix4f getGbufferProjectionInverse() {
		return invertedCopy(CapturedRenderingState.INSTANCE.getGbufferProjection());
	}

	private static int isEyeInWater() {
		Entity cameraEntity = Objects.requireNonNull(client.getCameraEntity());

		if (cameraEntity.isSubmergedInWater()) {
			return 1;
		} else if (cameraEntity.isInLava()) {
			return 2;
		} else {
			return 0;
		}
	}

	private static Matrix4f invertedCopy(Matrix4f matrix) {
		// PERF: Don't copy this matrix every time
		Matrix4f copy = matrix.copy();

		copy.invert();

		return copy;
	}
}
