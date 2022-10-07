package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;

public class VanillaUniforms {
	public static void addVanillaUniforms(DynamicUniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "iris_TextureMat", RenderSystem::getTextureMatrix);
		uniforms.uniform4fArray(UniformUpdateFrequency.PER_FRAME, "iris_ColorModulator", RenderSystem::getShaderColor);
		Vector2f cachedScreenSize = new Vector2f();
		uniforms.uniform1f("iris_LineWidth", RenderSystem::getShaderLineWidth, ExtendedShader.getShaderApplyNotifier());
		uniforms.uniform2f(UniformUpdateFrequency.PER_FRAME, "iris_ScreenSize", () -> cachedScreenSize.set(GlStateManager.Viewport.width(), GlStateManager.Viewport.height()));
	}
}
