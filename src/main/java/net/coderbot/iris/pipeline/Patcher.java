package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.shader.ShaderType;

public interface Patcher {
	static Patcher INSTANCE = new TransformPatcher();

	public String patchAttributes(String source, ShaderType type, boolean hasGeometry);
}
