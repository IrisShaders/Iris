package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.FogMode;
import org.lwjgl.opengl.GL11;

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

		uniforms.uniform1f("fogDensity", () -> {

			if (fogMode == FogMode.OFF) {
				return 0.0f;
			}

			float rd = CameraUniforms.getRenderDistanceInBlocks();

			return (rd * 0.05F) - (RenderSystem.getShaderFogEnd() * 2) / Math.min(rd, 192.0F) * rd;
		}, listener -> {
			StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
		});
	}
}
