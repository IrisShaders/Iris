package net.coderbot.iris.pipeline;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;

public interface WorldRenderingPipeline {
	void beginWorldRendering();
	void renderShadows(WorldRendererAccessor worldRenderer);
	void beginShadowRender();
	void endShadowRender();
	void beginTranslucents();
	void pushProgram(GbufferProgram program);
	void popProgram(GbufferProgram program);
	void finalizeWorldRendering();

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();
}
