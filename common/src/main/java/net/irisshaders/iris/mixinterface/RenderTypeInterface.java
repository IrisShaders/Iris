package net.irisshaders.iris.mixinterface;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface RenderTypeInterface {
	default RenderTarget iris$getRenderTarget() {
		throw new AssertionError("No accessible");
	}

	default RenderPipeline iris$getPipeline() {
		throw new AssertionError("No accessible");
	}
}
