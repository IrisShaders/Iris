package net.coderbot.iris.pipeline;

import net.coderbot.iris.uniforms.FrameUpdateNotifier;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	ShaderMap getShaderMap();
	FrameUpdateNotifier getFrameUpdateNotifier();

	boolean shouldOverrideShaders();
}
