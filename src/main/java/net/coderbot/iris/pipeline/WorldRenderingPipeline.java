package net.coderbot.iris.pipeline;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Camera;

import java.util.List;
import java.util.OptionalInt;

public interface WorldRenderingPipeline {
	void beginLevelRendering();
	void renderShadows(LevelRendererAccessor levelRenderer, Camera camera);
	void addDebugText(List<String> messages);
	OptionalInt getForcedShadowRenderDistanceChunksForDisplay();

	WorldRenderingPhase getPhase();

	void beginSodiumTerrainRendering();
	void endSodiumTerrainRendering();
	void setOverridePhase(WorldRenderingPhase phase);
	void setPhase(WorldRenderingPhase phase);
	void setInputs(InputAvailability availability);
	void setSpecialCondition(SpecialCondition special);
	void syncProgram();
	RenderTargetStateListener getRenderTargetStateListener();

	void beginHand();

	void beginTranslucents();
	void finalizeLevelRendering();
	void destroy();

	SodiumTerrainPipeline getSodiumTerrainPipeline();
	FrameUpdateNotifier getFrameUpdateNotifier();

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();
	boolean shouldRenderClouds();
	boolean shouldRenderUnderwaterOverlay();
	boolean shouldRenderVignette();
	boolean shouldWriteRainAndSnowToDepthBuffer();
	boolean shouldRenderParticlesBeforeDeferred();

	float getSunPathRotation();
}
