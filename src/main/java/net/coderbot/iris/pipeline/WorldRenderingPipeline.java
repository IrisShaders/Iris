package net.coderbot.iris.pipeline;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.minecraft.client.render.Camera;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;

public interface WorldRenderingPipeline {
	void beginWorldRendering();
	default void renderShadows(WorldRendererAccessor worldRenderer, Camera camera) {
	}
	void beginShadowRender();
	void endShadowRender();
	void beginTranslucents();
	void pushProgram(GbufferProgram program);
	void popProgram(GbufferProgram program);
	void finalizeWorldRendering();

	default void setPhase(WorldRenderingPhase phase) {
		// no-op
	}

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();

	default float getSunPathRotation() {
		return 0.0F;
	}
}
