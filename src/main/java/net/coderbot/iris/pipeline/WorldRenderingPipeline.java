package net.coderbot.iris.pipeline;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.newshader.FlwProgram;
import net.coderbot.iris.shaderpack.CloudSetting;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Camera;

import java.util.List;
import java.util.OptionalInt;

public interface WorldRenderingPipeline {
	void beginLevelRendering();
	void renderShadows(LevelRendererAccessor worldRenderer, Camera camera);
	void addDebugText(List<String> messages);
	OptionalInt getForcedShadowRenderDistanceChunksForDisplay();

	WorldRenderingPhase getPhase();

	void beginSodiumTerrainRendering();
	void endSodiumTerrainRendering();
	void setOverridePhase(WorldRenderingPhase phase);
	void setPhase(WorldRenderingPhase phase);
	void setSpecialCondition(SpecialCondition special);
	RenderTargetStateListener getRenderTargetStateListener();

	void beginHand();

	void beginTranslucents();
	void finalizeLevelRendering();
	void destroy();

	SodiumTerrainPipeline getSodiumTerrainPipeline();
	FrameUpdateNotifier getFrameUpdateNotifier();

	boolean shouldDisableVanillaEntityShadows();
	boolean shouldDisableDirectionalShading();
	CloudSetting getCloudSetting();
	boolean shouldRenderUnderwaterOverlay();
	boolean shouldRenderVignette();
	boolean shouldRenderSun();
	boolean shouldRenderMoon();
	boolean shouldWriteRainAndSnowToDepthBuffer();
	boolean shouldRenderParticlesBeforeDeferred();

	default FlwProgram getTerrainFlwProgram() {
		return null;
	}

	default FlwProgram getTerrainCutoutFlwProgram() {
		return null;
	}

	default FlwProgram getShadowFlwProgram() {
		return null;
	}

	default FlwProgram getShadowCutoutFlwProgram() {
		return null;
	}

	float getSunPathRotation();
}
