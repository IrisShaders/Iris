package net.irisshaders.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.irisshaders.iris.block_rendering.BlockRenderingSettings;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gbuffer_overrides.matching.InputAvailability;
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
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.OptionalInt;

public class FixedFunctionWorldRenderingPipeline implements WorldRenderingPipeline {
	public FixedFunctionWorldRenderingPipeline() {
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(false);
		BlockRenderingSettings.INSTANCE.setSeparateEntityDraws(false);
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(1.0f);
		BlockRenderingSettings.INSTANCE.setUseExtendedVertexFormat(false);
		BlockRenderingSettings.INSTANCE.setVoxelizeLightBlocks(false);
		BlockRenderingSettings.INSTANCE.setBlockTypeIds(null);
	}

	@Override
	public ShaderMap getShaderMap() {
		return null;
	}

	@Override
	public boolean shouldOverrideShaders() {
		return false;
	}

	@Override
	public void onShadowBufferChange() {

	}

	@Override
	public void beginLevelRendering() {
		// Use the default Minecraft framebuffer and ensure that no programs are in use
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
		GlStateManager._glUseProgram(0);
	}

	@Override
	public void renderShadows(LevelRendererAccessor worldRenderer, Camera camera) {
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
	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return Object2ObjectMaps.emptyMap();
	}

	@Override
	public WorldRenderingPhase getPhase() {
		return WorldRenderingPhase.NONE;
	}

	@Override
	public void setPhase(WorldRenderingPhase phase) {

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

	//@Override
	public void setInputs(InputAvailability availability) {

	}

	@Override
	public void setSpecialCondition(SpecialCondition special) {

	}

	//@Override
	public void syncProgram() {

	}

	@Override
	public RenderTargetStateListener getRenderTargetStateListener() {
		return RenderTargetStateListener.NOP;
	}

	@Override
	public int getCurrentNormalTexture() {
		return 0;
	}

	@Override
	public int getCurrentSpecularTexture() {
		return 0;
	}

	@Override
	public void onSetShaderTexture(int id) {

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
	public CloudSetting getCloudSetting() {
		return CloudSetting.DEFAULT;
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
	public boolean shouldRenderSun() {
		return true;
	}

	@Override
	public boolean shouldRenderMoon() {
		return true;
	}

	@Override
	public boolean shouldWriteRainAndSnowToDepthBuffer() {
		return false;
	}

	@Override
	public ParticleRenderingSettings getParticleRenderingSettings() {
		return ParticleRenderingSettings.MIXED;
	}

	@Override
	public boolean allowConcurrentCompute() {
		return false;
	}

	@Override
	public boolean hasFeature(FeatureFlags flags) {
		return false;
	}

	@Override
	public float getSunPathRotation() {
		// No sun tilt
		return 0;
	}

	@Override
	public void colorSpaceChanged() {

	}
}
