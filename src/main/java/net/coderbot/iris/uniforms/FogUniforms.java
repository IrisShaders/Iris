package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.opengl.GL11;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public class FogUniforms {
	private FogUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		if (fogMode == FogMode.OFF) {
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogMode", () -> 0);
		} else if (fogMode == FogMode.LINEAR) {
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogMode", () -> GL11.GL_LINEAR);
		}
		
		uniforms
				// TODO: Update frequency of continuous?
				.uniform3f(PER_FRAME, "fogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vec3f(fogColor[0], fogColor[1], fogColor[2]);
				})
				.uniform1f(PER_FRAME, "fogStart", RenderSystem::getShaderFogStart)
				.uniform1f(PER_FRAME, "fogEnd", RenderSystem::getShaderFogEnd)
				.uniform1f(PER_FRAME, "fogDensity", () -> 0)
				.uniform4f(PER_FRAME, "iris_FogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vector4f(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
				})
				.uniform1f(PER_FRAME, "iris_FogStart", RenderSystem::getShaderFogStart)
				.uniform1f(PER_FRAME, "iris_FogEnd", RenderSystem::getShaderFogEnd)
				.uniform1f(PER_FRAME, "iris_FogDensity", () -> 0); // TODO: Implement FogDensity and fogMode
	}
}
