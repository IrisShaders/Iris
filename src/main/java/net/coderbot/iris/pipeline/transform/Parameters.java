package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.job_parameter.JobParameters;
import net.coderbot.iris.gl.shader.ShaderType;

class Parameters extends JobParameters {
	public final Patch patch;
	public final ShaderType type;

	public Parameters(Patch patch, ShaderType type) {
		this.patch = patch;
		this.type = type;
	}

	@Override
	public boolean equals(JobParameters other) {
		return this == other;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
