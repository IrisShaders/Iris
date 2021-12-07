package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.vendored.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public class FogUniforms {
	private FogUniforms() {
		// no construction
	}

	public static void addFogUniforms(UniformHolder uniforms, FogMode fogMode) {
		if (fogMode == FogMode.OFF) {
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogMode", () -> 0);
		} else if (fogMode == FogMode.LINEAR) {
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogMode", () -> GL11.GL_LINEAR);
		}

		uniforms
				// TODO: Update frequency of continuous?
				.uniform3f(PER_FRAME, "fogColor", () -> {
					float[] fogColor = RenderSystem.getShaderFogColor();
					return new Vector3f(fogColor[0], fogColor[1], fogColor[2]);
				});

		//TODO: (1.17) Fix fog density
		/*uniforms.uniform1f("fogDensity", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.enable).getState()) {
				return 0.0f;
			}

			return GlStateManagerAccessor.getFOG().density;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
		});*/
	}
}
