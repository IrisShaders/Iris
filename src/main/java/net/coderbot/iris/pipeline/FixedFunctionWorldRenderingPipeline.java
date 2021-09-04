package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;

import java.util.List;
import java.util.OptionalInt;

public class FixedFunctionWorldRenderingPipeline implements WorldRenderingPipeline {
	public FixedFunctionWorldRenderingPipeline() {
		BlockRenderingSettings.INSTANCE.setDisableDirectionalShading(shouldDisableDirectionalShading());
		BlockRenderingSettings.INSTANCE.setUseSeparateAo(false);
		BlockRenderingSettings.INSTANCE.setAmbientOcclusionLevel(1.0f);
	}

	@Override
	public void beginWorldRendering() {
		// Use the default Minecraft framebuffer and ensure that no programs are in use
		MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
		GlStateManager._glUseProgram(0);
	}

	@Override
	public void renderShadows(WorldRendererAccessor worldRenderer, Camera camera) {
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
	public void beginShadowRender() {
		// stub: nothing to do here
	}

	@Override
	public void endShadowRender() {
		// stub: nothing to do here
	}

	@Override
	public void beginTranslucents() {
		// stub: nothing to do here
	}

	@Override
	public void pushProgram(GbufferProgram program) {
		// stub: nothing to do here
	}

	@Override
	public void popProgram(GbufferProgram program) {
		// stub: nothing to do here
	}

	@Override
	public void finalizeWorldRendering() {
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
	public float getSunPathRotation() {
		// No sun tilt
		return 0;
	}
}
