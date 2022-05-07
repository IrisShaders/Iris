package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.vendored.joml.Vector4f;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * Internal Iris uniforms that are not directly accessible by shaders.
 */
public class IrisInternalUniforms {
	private IrisInternalUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms) {
		uniforms
				// TODO: Update frequency of continuous?
				.uniform4f(PER_FRAME, "iris_FogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vector4f(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
				})
				.uniform1f(PER_FRAME, "iris_FogStart", RenderSystem::getShaderFogStart)
				.uniform1f(PER_FRAME, "iris_FogEnd", RenderSystem::getShaderFogEnd);

		uniforms.uniform1f("iris_FogDensity", () -> {
			// ensure that the minimum value is 0.0
			return Math.max(0.0F, CapturedRenderingState.INSTANCE.getFogDensity());
		}, notifier -> {});
	}
}
