package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PipelineManager {
	private final Function<NamespacedId, WorldRenderingPipeline> pipelineFactory;
	private final Map<NamespacedId, WorldRenderingPipeline> pipelinesPerDimension = new HashMap<>();
	private WorldRenderingPipeline pipeline = new VanillaRenderingPipeline();
	private int versionCounterForSodiumShaderReload = 0;

	public PipelineManager(Function<NamespacedId, WorldRenderingPipeline> pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	public WorldRenderingPipeline preparePipeline(NamespacedId currentDimension) {
		if (!pipelinesPerDimension.containsKey(currentDimension)) {
			SystemTimeUniforms.COUNTER.reset();
			SystemTimeUniforms.TIMER.reset();

			Iris.logger.info("Creating pipeline for dimension {}", currentDimension);
			pipeline = pipelineFactory.apply(currentDimension);
			pipelinesPerDimension.put(currentDimension, pipeline);

			if (WorldRenderingSettings.INSTANCE.isReloadRequired()) {
				if (Minecraft.getInstance().levelExtractor != null) {
					Minecraft.getInstance().levelExtractor.allChanged();
				}

				WorldRenderingSettings.INSTANCE.clearReloadRequired();
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

	/**
	 * In IrisChunkProgramOverrides#getProgramOverride,
	 * it uses version counter to check whether to reload sodium shaders.
	 * This fixes a compat issue with Immersive Portals(#1188).
	 * Immersive Portals may load multiple client dimensions at the same time,
	 * and every dimension corresponds to a IrisChunkProgramOverrides object.
	 * Multiple dimensions (mod dimensions that fallback to overworld shaders) may use the same pipeline.
	 * This ensures that the sodium shader for each dimension will get properly reloaded.
	 */
	public int getVersionCounterForSodiumShaderReload() {
		return versionCounterForSodiumShaderReload;
	}

	/**
	 * Destroys all the current pipelines.
	 *
	 * <p>This method is <b>EXTREMELY DANGEROUS!</b> It is a huge potential source of hard-to-trace inconsistencies
	 * in program state. You must make sure that you <i>immediately</i> re-prepare the pipeline after destroying
	 * it to prevent the program from falling into an inconsistent state.</p>
	 *
	 * <p>In particular, </p>
	 *
	 * @see <a href="https://github.com/IrisShaders/Iris/issues/1330">this GitHub issue</a>
	 */
	public void destroyPipeline() {
		pipelinesPerDimension.forEach((dimensionId, pipeline) -> {
			Iris.logger.info("Destroying pipeline {}", dimensionId);
			pipeline.destroy();
		});

		pipelinesPerDimension.clear();
		pipeline = null;
		versionCounterForSodiumShaderReload++;
	}

}
