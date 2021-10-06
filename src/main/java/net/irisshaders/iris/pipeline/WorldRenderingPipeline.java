package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.layer.GbufferProgram;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.minecraft.client.Camera;
import java.util.List;
import java.util.OptionalInt;

public interface WorldRenderingPipeline {
	void beginLevelRendering();
	void renderShadows(LevelRendererAccessor levelRenderer, Camera camera);
	void addDebugText(List<String> messages);
	OptionalInt getForcedShadowRenderDistanceChunksForDisplay();
	void beginShadowRender();
	void endShadowRender();
	void beginTranslucents();
	void pushProgram(GbufferProgram program);
	void popProgram(GbufferProgram program);
	void finalizeLevelRendering();
	void destroy();

	SodiumTerrainPipeline getSodiumTerrainPipeline();

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();
	boolean shouldRenderClouds();

	float getSunPathRotation();
}
