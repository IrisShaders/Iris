package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;

public class ComputeParameters extends Parameters {
	public ComputeParameters(Patch patch) {
		super(patch);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}
}
