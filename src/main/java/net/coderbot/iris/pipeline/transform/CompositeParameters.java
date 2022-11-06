package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.UniformBufferObject;

public class CompositeParameters extends Parameters {
	public CompositeParameters(Patch patch, UniformBufferObject bufferObject) {
		super(patch, bufferObject);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}
}
