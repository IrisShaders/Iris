package net.coderbot.iris.pipeline;

import net.coderbot.iris.shaderpack.ProgramSet;

public class InternalLevelRenderingPipeline extends DeferredLevelRenderingPipeline {
	public InternalLevelRenderingPipeline(ProgramSet programs) {
		super(programs);
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return false;
	}
}
