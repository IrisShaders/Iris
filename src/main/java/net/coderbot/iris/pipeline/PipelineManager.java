package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL20C;

import java.util.function.Function;

public class PipelineManager {
	private static PipelineManager instance;
	private Function<DimensionId, WorldRenderingPipeline> pipelineFactory;
	private WorldRenderingPipeline pipeline;
	private DimensionId lastDimension;

	/**
	 * Controls whether directional shading was previously disabled
	 */
	private boolean wasDisablingDirectionalShading;

	public PipelineManager(Function<DimensionId, WorldRenderingPipeline> pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
		this.wasDisablingDirectionalShading = DirectionalShadingHelper.shouldDisableDirectionalShading;
	}

	public WorldRenderingPipeline preparePipeline(DimensionId currentDimension) {
		if (currentDimension != lastDimension) {
			Iris.logger.info("Reloading shaderpack on dimension change (" + lastDimension + " -> " + currentDimension + ")");

			lastDimension = currentDimension;
			destroyPipeline();
		}

		if (pipeline == null) {
			// Ensure that the timers are reset
			SystemTimeUniforms.COUNTER.reset();
			SystemTimeUniforms.TIMER.reset();

			pipeline = pipelineFactory.apply(lastDimension);

			boolean disableDirectionalShading = pipeline.shouldDisableDirectionalShading();

			if (wasDisablingDirectionalShading != disableDirectionalShading) {
				// Re-render all of the chunks due to the change in directional shading setting
				DirectionalShadingHelper.shouldDisableDirectionalShading = disableDirectionalShading;
				wasDisablingDirectionalShading = disableDirectionalShading;
			}

			// TODO: Do not always reload on shaderpack changes, and only reload if the block ID mapping changes
			//
			// If the block ID mapping changes and the world render is not reloaded, then things won't work correctly.
			MinecraftClient.getInstance().worldRenderer.reload();

			// If Sodium is loaded, we need to reload the world renderer to properly recreate the ChunkRenderBackend
			// Otherwise, the terrain shaders won't be changed properly.
			// We also need to re-render all of the chunks if there is a change in the directional shading setting
			//
			// TODO: Don't trigger a reload if this is the first time the world is being rendered
			/*if (FabricLoader.getInstance().isModLoaded("sodium")) {
				MinecraftClient.getInstance().worldRenderer.reload();
			}*/
		}

		return pipeline;
	}

	public WorldRenderingPipeline getPipeline() {
		return pipeline;
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
			GlStateManager.activeTexture(GL20C.GL_TEXTURE0 + i);
			GlStateManager.bindTexture(0);
		}

		// Set the active texture unit to unit 0
		//
		// This seems to be what most code expects. It's a sane default in any case.
		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);

		// Destroy the old world rendering pipeline
		//
		// This destroys all loaded shader programs and all of the render targets.
		if (pipeline instanceof DeferredWorldRenderingPipeline) {
			// TODO: Don't cast this to DeferredWorldRenderingPipeline?
			((DeferredWorldRenderingPipeline) pipeline).destroy();
		}

		pipeline = null;
	}
}
