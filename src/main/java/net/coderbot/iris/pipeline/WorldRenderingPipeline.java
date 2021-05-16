package net.coderbot.iris.pipeline;

import net.coderbot.iris.layer.GbufferProgram;

public interface WorldRenderingPipeline {
	void beginWorldRendering();
	void beginTranslucents();
	void pushProgram(GbufferProgram program);
	void popProgram(GbufferProgram program);
	void finalizeWorldRendering();

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();

	default float getSunPathRotation() {
		return 0.0F;
	}
}
