package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.jetbrains.annotations.Nullable;

public interface FrontendRenderPipelineAccess {
	public @Nullable RenderPipeline iris$getSource();

	public void iris$setSource(RenderPipeline source);
}
