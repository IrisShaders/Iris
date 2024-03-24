package net.irisshaders.iris.pipeline.transform;

import net.irisshaders.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX, ".vsh"),
	GEOMETRY(ShaderType.GEOMETRY, ".gsh"),
	TESS_CONTROL(ShaderType.TESSELATION_CONTROL, ".tcs"),
	TESS_EVAL(ShaderType.TESSELATION_EVAL, ".tes"),
	FRAGMENT(ShaderType.FRAGMENT, ".fsh"),
	COMPUTE(ShaderType.COMPUTE, ".csh");

	public final ShaderType glShaderType;
	public final String extension;

	PatchShaderType(ShaderType glShaderType, String extension) {
		this.glShaderType = glShaderType;
		this.extension = extension;
	}

	public static PatchShaderType[] fromGlShaderType(ShaderType glShaderType) {
		return switch (glShaderType) {
			case VERTEX -> new PatchShaderType[]{VERTEX};
			case GEOMETRY -> new PatchShaderType[]{GEOMETRY};
			case TESSELATION_CONTROL -> new PatchShaderType[]{TESS_CONTROL};
			case TESSELATION_EVAL -> new PatchShaderType[]{TESS_EVAL};
			case COMPUTE -> new PatchShaderType[]{COMPUTE};
			case FRAGMENT -> new PatchShaderType[]{FRAGMENT};
			default -> throw new IllegalArgumentException("Unknown shader type: " + glShaderType);
		};
	}
}
