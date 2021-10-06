package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.shaderpack.ProgramSet;

public class InternalWorldRenderingPipeline extends DeferredWorldRenderingPipeline {
	public InternalWorldRenderingPipeline(ProgramSet programs) {
		super(programs);
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return false;
	}
}
