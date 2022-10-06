package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

public class VanillaUniforms {
	public static void addVanillaUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "iris_ProjMat", RenderSystem::getProjectionMatrix);
		uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "iris_ModelViewMat", RenderSystem::getModelViewMatrix);
		uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "iris_TextureMat", RenderSystem::getTextureMatrix);
		uniforms.uniform4fArray(UniformUpdateFrequency.PER_FRAME, "iris_ColorModulator", RenderSystem::getShaderColor);
	}
}
