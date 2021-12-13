package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PipelineManager {
	private static PipelineManager instance;
	private final Function<DimensionId, WorldRenderingPipeline> pipelineFactory;
	private final Map<DimensionId, WorldRenderingPipeline> pipelinesPerDimension = new HashMap<>();
	private WorldRenderingPipeline pipeline;
	private boolean sodiumShaderReloadNeeded;

	public PipelineManager(Function<DimensionId, WorldRenderingPipeline> pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	public WorldRenderingPipeline preparePipeline(DimensionId currentDimension) {
		if (!pipelinesPerDimension.containsKey(currentDimension)) {
			SystemTimeUniforms.COUNTER.reset();
			SystemTimeUniforms.TIMER.reset();

			Iris.logger.info("Creating pipeline for dimension {}", currentDimension);
			pipeline = pipelineFactory.apply(currentDimension);
			pipelinesPerDimension.put(currentDimension, pipeline);
			sodiumShaderReloadNeeded = true;

			if (BlockRenderingSettings.INSTANCE.isReloadRequired()) {
				Minecraft.getInstance().levelRenderer.allChanged();
				BlockRenderingSettings.INSTANCE.clearReloadRequired();
			}
		} else {
			pipeline = pipelinesPerDimension.get(currentDimension);
		}

		return pipeline;
	}

	@Nullable
	public WorldRenderingPipeline getPipelineNullable() {
		return pipeline;
	}

	public Optional<WorldRenderingPipeline> getPipeline() {
		return Optional.ofNullable(pipeline);
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
		pipelinesPerDimension.forEach((dimensionId, pipeline) -> {
			Iris.logger.info("Destroying pipeline {}", dimensionId);
			resetTextureState();
			pipeline.destroy();
		});

		pipelinesPerDimension.clear();
		pipeline = null;
	}

	private void resetTextureState() {
		// Unbind all textures
		//
		// This is necessary because we don't want destroyed render target textures to remain bound to certain texture
		// units. Vanilla appears to properly rebind all textures as needed, and we do so too, so this does not cause
		// issues elsewhere.
		//
		// Without this code, there will be weird issues when reloading certain shaderpacks.
		for (int i = 0; i < 16; i++) {
			GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0 + i);
			GlStateManager._bindTexture(0);
		}

		// Set the active texture unit to unit 0
		//
		// This seems to be what most code expects. It's a sane default in any case.
		GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0);
	}
}
