package net.irisshaders.iris.platform;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface PipelineBuilderStorage {
	void copyPipelineShaderFrom(RenderPipeline pipeline);
}
