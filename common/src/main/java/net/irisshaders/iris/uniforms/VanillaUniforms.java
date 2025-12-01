package net.irisshaders.iris.uniforms;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

public class VanillaUniforms {
	public static void addVanillaUniforms(DynamicUniformHolder uniforms) {
		Vector2f cachedScreenSize = new Vector2f();
		// listener -> {} dictates we want this to run on every shader update, not just on a new frame. These are dynamic.
		uniforms.uniform1f("iris_LineWidth", RenderSystem::getShaderLineWidth, listener -> {
		});
		// TODO 1.21.5: Not correct. Should be using viewport size.
		uniforms.uniform2f("iris_ScreenSize", () -> cachedScreenSize.set(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height), listener -> {
		});
	}
}
