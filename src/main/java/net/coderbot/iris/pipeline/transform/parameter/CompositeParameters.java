package net.coderbot.iris.pipeline.transform.parameter;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.transform.Patch;

public class CompositeParameters extends Parameters {
	public CompositeParameters(Patch patch) {
		super(patch);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}
}
