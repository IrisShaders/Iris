package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20C;

import java.util.function.Function;

public class PipelineManager {
	private static PipelineManager instance;
	private final Function<DimensionId, WorldRenderingPipeline> pipelineFactory;
	private WorldRenderingPipeline pipeline;
	private boolean sodiumShaderReloadNeeded;
	private DimensionId lastDimension;

	public PipelineManager(Function<DimensionId, WorldRenderingPipeline> pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	public WorldRenderingPipeline preparePipeline(DimensionId currentDimension) {
		if (currentDimension != lastDimension) {
			// TODO: Don't say anything about compiling shaders if shaders are disabled.
			if (lastDimension == null) {
				Iris.logger.info("Compiling shaderpack on initial world load (for dimension: " + currentDimension + ")");
			} else {
				Iris.logger.info("Recompiling shaderpack on dimension change (" + lastDimension + " -> " + currentDimension + ")");
			}

			lastDimension = currentDimension;
			destroyPipeline();
		}

		if (pipeline == null) {
			// Ensure that the timers are reset
			SystemTimeUniforms.COUNTER.reset();
			SystemTimeUniforms.TIMER.reset();

			pipeline = pipelineFactory.apply(lastDimension);
			sodiumShaderReloadNeeded = true;

			// If Sodium is loaded, we need to reload the world renderer to properly recreate the ChunkRenderBackend
			// Otherwise, the terrain shaders won't be changed properly.
			// We also need to re-render all of the chunks if there is a change in the directional shading setting,
			// ID mapping, or separateAo setting.
			//
			// TODO: Don't trigger a reload if this is the first time the world is being rendered
			if (BlockRenderingSettings.INSTANCE.isReloadRequired()) {
				Minecraft.getInstance().levelRenderer.allChanged();
				BlockRenderingSettings.INSTANCE.clearReloadRequired();
			}
		}

		return pipeline;
	}

	public WorldRenderingPipeline getPipeline() {
		return pipeline;
	}

	public boolean isSodiumShaderReloadNeeded() {
		return sodiumShaderReloadNeeded;
	}

	public void clearSodiumShaderReloadNeeded() {
		sodiumShaderReloadNeeded = false;
	}

	public void setAsInstance() {
		if (instance != null) {
			throw new IllegalStateException("Multiple pipeline managers active at one time");
		} else {
			instance = this;
		}
	}

	public static void resetInstance() {
		instance = null;
	}

	public static PipelineManager getInstance() {
		return instance;
	}

	public void destroyPipeline() {
		// Unbind all textures
		//
		// This is necessary because we don't want destroyed render target textures to remain bound to certain texture
		// units. Vanilla appears to properly rebind all textures as needed, and we do so too, so this does not cause
		// issues elsewhere.
		//
		// Without this code, there will be weird issues when reloading certain shaderpacks.
		for (int i = 0; i < 16; i++) {
			GlStateManager._activeTexture(GL20C.GL_TEXTURE0 + i);
			GlStateManager._bindTexture(0);
		}

		// Set the active texture unit to unit 0
		//
		// This seems to be what most code expects. It's a sane default in any case.
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		// Destroy the old world rendering pipeline
		//
		// This destroys all loaded shader programs and all of the render targets.
		if (pipeline != null) {
			pipeline.destroy();
		}

		pipeline = null;
	}
}
