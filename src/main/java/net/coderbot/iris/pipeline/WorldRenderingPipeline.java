package net.coderbot.iris.pipeline;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.minecraft.client.render.Camera;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;

import java.util.List;

public interface WorldRenderingPipeline {
	void beginWorldRendering();
	default void renderShadows(WorldRendererAccessor worldRenderer, Camera camera) {
	}
	default void addDebugText(List<String> messages) {
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
