package net.irisshaders.iris.uniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.util.FogStorage;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public class FogUniforms {
	private FogUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		if (fogMode == FogMode.OFF) {
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogMode", () -> 0);
			uniforms.uniform1i(UniformUpdateFrequency.ONCE, "fogShape", () -> -1);
		} else if (fogMode == FogMode.PER_VERTEX || fogMode == FogMode.PER_FRAGMENT) {
			uniforms.uniform1i("fogMode", () -> {
				float fogDensity = CapturedRenderingState.INSTANCE.getFogDensity();

				if (fogDensity < 0.0F) {
					return GL11.GL_LINEAR;
				} else {
					return GL11.GL_EXP2;
				}
			}, listener -> {
			});

			// To keep a stable interface, 0 is defined as spherical while 1 is defined as cylindrical, even if Mojang's index changes.
			uniforms.uniform1i(PER_FRAME, "fogShape", () -> 1);
		}

		uniforms.uniform1f("fogDensity", () -> {
			// ensure that the minimum value is 0.0
			return Math.max(0.0F, CapturedRenderingState.INSTANCE.getFogDensity());
		}, notifier -> {
		});

		uniforms.uniform1f("fogStart", () -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalStart(), listener -> StateUpdateNotifiers.fogStartNotifier.setListener(listener));

		uniforms.uniform1f("fogEnd", () -> ((FogStorage) Minecraft.getInstance().gameRenderer).sodium$getFogParameters().environmentalEnd(), listener -> StateUpdateNotifiers.fogEndNotifier.setListener(listener));

		uniforms
			// TODO: Update frequency of continuous?
			.uniform3f(PER_FRAME, "fogColor", () -> {
				return new Vector3f((float) CapturedRenderingState.INSTANCE.getFogColor().x, (float) CapturedRenderingState.INSTANCE.getFogColor().y, (float) CapturedRenderingState.INSTANCE.getFogColor().z);
			});
	}
}
