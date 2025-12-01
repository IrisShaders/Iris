package net.irisshaders.iris.platform;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface PipelineBuilderStorage {
	void copyPipelineShaderFrom(RenderPipeline pipeline);
}
