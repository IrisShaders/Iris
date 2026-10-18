package net.irisshaders.iris.backend;

import net.irisshaders.iris.pipeline.transform.PatchedShader;

public interface GraphicsShader extends AutoCloseable {
	PatchedShader getSource();

	GraphicsPipeline createPipeline(PipelineState state);

	default GraphicsPipeline createPipeline(PipelineState state, boolean background) {
		return this.createPipeline(state);
	}

	default void cancelCompilation() {
	}

	@Override
	void close();
}
