package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class IrisExclusiveUniforms {
	public static void addIrisUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_TICK, "dragonDeathProgress", IrisExclusiveUniforms::getDragonDeathProgress);
	}

	private static float getDragonDeathProgress() {
		return CapturedRenderingState.INSTANCE.getTicksSinceDragonDeath() / 200;
	}
}
