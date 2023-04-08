package net.irisshaders.iris.pipeline.transform;

import net.irisshaders.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX),
	GEOMETRY(ShaderType.GEOMETRY),
	FRAGMENT(ShaderType.FRAGMENT),
	COMPUTE(ShaderType.COMPUTE);

	public final ShaderType glShaderType;

	PatchShaderType(ShaderType glShaderType) {
		this.glShaderType = glShaderType;
	}

	public static PatchShaderType[] fromGlShaderType(ShaderType glShaderType) {
		switch (glShaderType) {
			case VERTEX:
				return new PatchShaderType[]{VERTEX};
			case GEOMETRY:
				return new PatchShaderType[]{GEOMETRY};
			case COMPUTE:
				return new PatchShaderType[]{COMPUTE};
			case FRAGMENT:
				return new PatchShaderType[]{FRAGMENT};
			default:
				throw new IllegalArgumentException("Unknown shader type: " + glShaderType);
		}
	}
}
