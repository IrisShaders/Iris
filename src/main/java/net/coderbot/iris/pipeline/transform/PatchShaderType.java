package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX, ".vsh"),
	GEOMETRY(ShaderType.GEOMETRY, ".gsh"),
	FRAGMENT(ShaderType.FRAGMENT, ".fsh"),
	COMPUTE(ShaderType.COMPUTE, ".csh");

	public final ShaderType glShaderType;
	public final String extension;

	private PatchShaderType(ShaderType glShaderType, String extension) {
		this.glShaderType = glShaderType;
		this.extension = extension;
	}

	public static PatchShaderType[] fromGlShaderType(ShaderType glShaderType) {
		switch (glShaderType) {
			case VERTEX:
				return new PatchShaderType[] { VERTEX };
			case GEOMETRY:
				return new PatchShaderType[] { GEOMETRY };
			case COMPUTE:
				return new PatchShaderType[] { COMPUTE };
			case FRAGMENT:
				return new PatchShaderType[] { FRAGMENT };
			default:
				throw new IllegalArgumentException("Unknown shader type: " + glShaderType);
		}
	}
}
