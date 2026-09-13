package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

public class VanillaUniforms {
	public static void addVanillaUniforms(UniformHolder uniforms) {
		Vector2f cachedScreenSize = new Vector2f();
		// TODO 1.21.5: Not correct. Should be using viewport size.
		uniforms.uniform2f(UniformUpdateFrequency.PER_FRAME, "iris_ScreenSize",
			() -> cachedScreenSize.set(Minecraft.getInstance().gameRenderer.mainRenderTarget().width,
				Minecraft.getInstance().gameRenderer.mainRenderTarget().height));
	}
}
