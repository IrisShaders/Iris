package net.coderbot.iris.pipeline;

import net.coderbot.iris.shaderpack.ProgramSet;

public class InternalWorldRenderingPipeline extends DeferredWorldRenderingPipeline {
	public InternalWorldRenderingPipeline(ProgramSet programs) {
		super(programs);
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return false;
	}
}
