package net.irisshaders.iris.backend;

import net.irisshaders.iris.pipeline.transform.PatchedShader;

public interface GraphicsShader extends AutoCloseable {
	PatchedShader getSource();

	GraphicsPipeline createPipeline(PipelineState state);

	@Override
	void close();
}
