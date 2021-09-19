package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.coderbot.iris.gl.uniform.UniformHolder;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public class FogUniforms117 {
	private FogUniforms117() {
		// no construction
	}

	public static void addFogUniforms(UniformHolder uniforms) {
		uniforms
				// TODO: Update frequency of continuous?
				.uniform3f(PER_FRAME, "fogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vector3f(fogColor[0], fogColor[1], fogColor[2]);
				})
				.uniform4f(PER_FRAME, "iris_FogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vector4f(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
				})
				.uniform1f(PER_FRAME, "iris_FogStart", RenderSystem::getShaderFogStart)
				.uniform1f(PER_FRAME, "iris_FogEnd", RenderSystem::getShaderFogEnd)
				.uniform1f(PER_FRAME, "iris_FogDensity", () -> 0); // TODO: Implement FogDensity and fogMode
	}
}
