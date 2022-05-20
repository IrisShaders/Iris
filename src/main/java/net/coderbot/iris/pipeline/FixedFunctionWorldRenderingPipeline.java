package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.OptionalInt;

public class FixedFunctionWorldRenderingPipeline implements WorldRenderingPipeline {
	public FixedFunctionWorldRenderingPipeline() {
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(false);
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(1.0f);
		BlockRenderingSettings.INSTANCE.setUseExtendedVertexFormat(false);
	}

	@Override
	public void beginLevelRendering() {
		// Use the default Minecraft framebuffer and ensure that no programs are in use
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
		GlStateManager._glUseProgram(0);
	}

	@Override
	public void renderShadows(LevelRendererAccessor levelRenderer, Camera camera) {
		// stub: nothing to do here
	}

	@Override
	public void addDebugText(List<String> messages) {
		// stub: nothing to do here
	}

	@Override
	public OptionalInt getForcedShadowRenderDistanceChunksForDisplay() {
		return OptionalInt.empty();
	}

	@Override
	public WorldRenderingPhase getPhase() {
		return WorldRenderingPhase.NONE;
	}

	@Override
	public void beginSodiumTerrainRendering() {

	}

	@Override
	public void endSodiumTerrainRendering() {

	}

	@Override
	public void setOverridePhase(WorldRenderingPhase phase) {

	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {

	}

	@Override
	public void setInputs(InputAvailability availability) {

	}

	@Override
	public void setSpecialCondition(SpecialCondition special) {

	}

	@Override
	public void syncProgram() {

	}

	@Override
	public RenderTargetStateListener getRenderTargetStateListener() {
		return RenderTargetStateListener.NOP;
	}

	@Override
	public void beginHand() {
	    // stub: nothing to do here
	}

	@Override
	public void beginTranslucents() {
		// stub: nothing to do here
	}

	@Override
	public void finalizeLevelRendering() {
		// stub: nothing to do here
	}

	@Override
	public void destroy() {
		// stub: nothing to do here
	}

	@Override
	public SodiumTerrainPipeline getSodiumTerrainPipeline() {
		// no shaders to override
		return null;
	}

	@Override
	public FrameUpdateNotifier getFrameUpdateNotifier() {
		// return a dummy notifier
		return new FrameUpdateNotifier();
	}

	@Override
	public boolean shouldDisableVanillaEntityShadows() {
		return false;
	}

	@Override
	public boolean shouldDisableDirectionalShading() {
		return false;
	}

	@Override
	public boolean shouldRenderClouds() {
		// Keep clouds enabled
		return true;
	}

	@Override
	public boolean shouldRenderUnderwaterOverlay() {
		return true;
	}

	@Override
	public boolean shouldRenderVignette() {
		return true;
	}

	@Override
	public boolean shouldWriteRainAndSnowToDepthBuffer() {
		return false;
	}

	@Override
	public boolean shouldRenderParticlesBeforeDeferred() {
		return false;
	}

	@Override
	public float getSunPathRotation() {
		// No sun tilt
		return 0;
	}
}
