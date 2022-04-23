package net.coderbot.iris.uniforms;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;
import net.coderbot.iris.mixin.statelisteners.GlStateManagerAccessor;

public class FogUniforms {
	private FogUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms) {
		uniforms.uniform1i("fogMode", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.enable).isEnabled()) {
				return 0;
			}

			return GlStateManagerAccessor.getFOG().mode;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogModeNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogDensity", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.enable).isEnabled()) {
				return 0.0f;
			}

			return GlStateManagerAccessor.getFOG().density;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogStart", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.enable).isEnabled()) {
				return 0.0f;
			}

			return GlStateManagerAccessor.getFOG().start;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogStartNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogEnd", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.enable).isEnabled()) {
				return 0.0f;
			}

			return GlStateManagerAccessor.getFOG().end;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogEndNotifier.setListener(listener);
		});
	}
}
