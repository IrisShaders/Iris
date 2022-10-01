package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;

public class CompositeParameters extends Parameters {
	public CompositeParameters(Patch patch) {
		super(patch);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}

	public static class ComputeParameters extends CompositeParameters {
		public ComputeParameters(Patch patch) {
			super(patch);
		}
	}
}
