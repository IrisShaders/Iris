package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class IrisUniforms {
	public static void addIrisUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_TICK, "dragonDeathProgress", CapturedRenderingState.INSTANCE::getTicksSinceDragonDeath);

	}
}
