package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.uniforms.FrameUpdateNotifier;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	ShaderMap getShaderMap();

	FrameUpdateNotifier getFrameUpdateNotifier();

	boolean shouldOverrideShaders();
}
