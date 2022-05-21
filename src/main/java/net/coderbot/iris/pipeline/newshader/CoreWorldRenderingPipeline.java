package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	ShaderMap getShaderMap();
	FrameUpdateNotifier getFrameUpdateNotifier();
	void destroy();

	boolean shouldOverrideShaders();
}
