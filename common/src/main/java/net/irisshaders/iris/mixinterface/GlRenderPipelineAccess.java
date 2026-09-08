package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.backend.api.BackendRenderPipeline;

public interface GlRenderPipelineAccess {
	BackendRenderPipeline.CreateInfo getCreateInfo();
}
