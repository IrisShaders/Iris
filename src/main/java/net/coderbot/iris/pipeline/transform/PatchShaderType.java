package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX),
	GEOMETRY(ShaderType.GEOMETRY),
	FRAGMENT(ShaderType.FRAGMENT),
	FRAGMENT_CUTOUT(ShaderType.FRAGMENT);

	public final ShaderType glShaderType;

	private PatchShaderType(ShaderType glShaderType) {
		this.glShaderType = glShaderType;
	}
}
