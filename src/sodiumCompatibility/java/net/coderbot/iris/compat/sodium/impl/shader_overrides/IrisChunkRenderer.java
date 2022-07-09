package net.coderbot.iris.compat.sodium.impl.shader_overrides;

public interface IrisChunkRenderer {
	void deletePipeline();
	void createPipelines(IrisChunkProgramOverrides overrides);

	int getMaxBatchSize();
}
