package net.irisshaders.iris.pipeline.newshader;

import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	ShaderMap getShaderMap();

	FrameUpdateNotifier getFrameUpdateNotifier();

	void destroy();

	boolean shouldOverrideShaders();
}
