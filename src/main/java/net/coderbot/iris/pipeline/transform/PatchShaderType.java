package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.shader.ShaderType;

public enum PatchShaderType {
	VERTEX(ShaderType.VERTEX),
	GEOMETRY(ShaderType.GEOMETRY),
	FRAGMENT(ShaderType.FRAGMENT),
	FRAGMENT_CUTOUT(ShaderType.FRAGMENT),
	COMPUTE(ShaderType.COMPUTE);

	public final ShaderType glShaderType;

	private PatchShaderType(ShaderType glShaderType) {
		this.glShaderType = glShaderType;
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
				return new PatchShaderType[] { FRAGMENT, FRAGMENT_CUTOUT };
			default:
				throw new IllegalArgumentException("Unknown shader type: " + glShaderType);
		}
	}
}
