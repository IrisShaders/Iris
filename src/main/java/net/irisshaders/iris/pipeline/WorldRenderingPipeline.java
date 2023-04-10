package net.irisshaders.iris.pipeline;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gbuffer_overrides.matching.SpecialCondition;
import net.irisshaders.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shaderpack.CloudSetting;
import net.irisshaders.iris.shaderpack.ParticleRenderingSettings;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Camera;

import java.util.List;
import java.util.OptionalInt;

public interface WorldRenderingPipeline {
	ShaderMap getShaderMap();

	boolean shouldOverrideShaders();

	void onShadowBufferChange();

	void beginLevelRendering();

	void renderShadows(LevelRendererAccessor worldRenderer, Camera camera);

	void addDebugText(List<String> messages);

	OptionalInt getForcedShadowRenderDistanceChunksForDisplay();

	Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap();

	WorldRenderingPhase getPhase();

	void setPhase(WorldRenderingPhase phase);

	void beginSodiumTerrainRendering();

	void endSodiumTerrainRendering();

	void setOverridePhase(WorldRenderingPhase phase);

	void setSpecialCondition(SpecialCondition special);

	RenderTargetStateListener getRenderTargetStateListener();

	int getCurrentNormalTexture();

	int getCurrentSpecularTexture();

	void onSetShaderTexture(int id);

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

	ParticleRenderingSettings getParticleRenderingSettings();

	boolean allowConcurrentCompute();

	boolean hasFeature(FeatureFlags flags);

	float getSunPathRotation();

	void colorSpaceChanged();
}
