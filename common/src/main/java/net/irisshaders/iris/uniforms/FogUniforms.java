package net.irisshaders.iris.uniforms;

import net.caffeinemc.mods.sodium.client.util.FogStorage;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class FogUniforms {
	public static void addFrameUniforms(UniformHolder uniforms) {
		uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "fogMode",
			() -> CapturedRenderingState.INSTANCE.getFogDensity() < 0.0F ? GL11.GL_LINEAR : GL11.GL_EXP2);
		// To keep a stable interface, 0 is defined as spherical while 1 is defined as cylindrical, even if Mojang's index changes.
		uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "fogShape", () -> 1);
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "fogDensity", () -> {
			// ensure that the minimum value is 0.0
			return Math.max(0.0F, CapturedRenderingState.INSTANCE.getFogDensity());
		});

		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "fogStart",
			() -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalStart());
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "fogEnd",
			() -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalEnd());

		addFogColor(uniforms);
	}

	public static void addFogColor(UniformHolder uniforms) {
		uniforms.uniform3f(UniformUpdateFrequency.PER_FRAME, "fogColor", () -> {
			return new Vector3f((float) CapturedRenderingState.INSTANCE.getFogColor().x,
				(float) CapturedRenderingState.INSTANCE.getFogColor().y, (float) CapturedRenderingState.INSTANCE.getFogColor().z);
		});
	}
}
