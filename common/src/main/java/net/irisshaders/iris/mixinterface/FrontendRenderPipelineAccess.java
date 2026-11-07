package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.jetbrains.annotations.Nullable;

public interface FrontendRenderPipelineAccess {
	@Nullable RenderPipeline iris$getSource();

	void iris$setSource(RenderPipeline source);
}
