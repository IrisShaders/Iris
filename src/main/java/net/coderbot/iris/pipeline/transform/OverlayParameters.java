package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

class OverlayParameters extends Parameters {
	public final boolean hasGeometry;

	public OverlayParameters(Patch patch, ShaderType type, boolean hasGeometry) {
		super(patch, type);
		this.hasGeometry = hasGeometry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasGeometry ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OverlayParameters other = (OverlayParameters) obj;
		if (hasGeometry != other.hasGeometry)
			return false;
		return true;
	}
}
