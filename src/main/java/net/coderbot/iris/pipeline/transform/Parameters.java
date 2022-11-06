package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.job_parameter.JobParameters;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.UniformBufferObject;

class Parameters extends JobParameters{
	public final Patch patch;
	public final UniformBufferObject bufferObject;
	public PatchShaderType type;

	public Parameters(Patch patch, UniformBufferObject bufferObject) {
		this.patch = patch;
		this.bufferObject = bufferObject;
	}

	public AlphaTest getAlphaTest() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((patch == null) ? 0 : patch.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameters other = (Parameters) obj;
		if (patch != other.patch)
			return false;
		return true;
	}
}
