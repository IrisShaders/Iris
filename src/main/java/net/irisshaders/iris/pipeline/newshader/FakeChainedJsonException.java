package net.irisshaders.iris.pipeline.newshader;

import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.minecraft.server.ChainedJsonException;

public class FakeChainedJsonException extends ChainedJsonException {
	private final ShaderCompileException trueException;

	public FakeChainedJsonException(ShaderCompileException e) {
		super("");
		this.trueException = e;
	}

	public ShaderCompileException getTrueException() {
		return trueException;
	}
}
