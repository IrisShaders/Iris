package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

class OverlayParameters extends Parameters {
	public final boolean hasGeometry;

	public OverlayParameters(Patch patch, ShaderType type, boolean hasGeometry) {
		super(patch, type);
		this.hasGeometry = hasGeometry;
	}
}
