package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX, ".vsh"),
	GEOMETRY(ShaderType.GEOMETRY, ".gsh"),
	TESS_CONTROL(ShaderType.TESSELATION_CONTROL, ".tcs"),
	TESS_EVAL(ShaderType.TESSELATION_EVAL, ".tes"),
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
			case TESSELATION_CONTROL:
				return new PatchShaderType[] { TESS_CONTROL };
			case TESSELATION_EVAL:
				return new PatchShaderType[] { TESS_EVAL };
			case COMPUTE:
				return new PatchShaderType[] { COMPUTE };
			case FRAGMENT:
				return new PatchShaderType[] { FRAGMENT };
			default:
				throw new IllegalArgumentException("Unknown shader type: " + glShaderType);
		}
	}
}
