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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((patch == null) ? 0 : patch.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Parameters other = (Parameters) obj;
		if (patch != other.patch)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public boolean equals(JobParameters other) {
		throw new UnsupportedOperationException("Not using CST transformer.");
	}
}
