package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.FogMode;
import org.lwjgl.opengl.GL11;

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

		//TODO: (1.17) Fix fog density
		/*uniforms.uniform1f("fogDensity", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((CapabilityTrackerAccessor) fog.capState).getState()) {
				return 0.0f;
			}

			return GlStateManagerAccessor.getFOG().density;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
		});*/
	}
}
