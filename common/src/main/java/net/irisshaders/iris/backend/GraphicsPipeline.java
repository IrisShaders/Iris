package net.irisshaders.iris.backend;

public interface GraphicsPipeline extends AutoCloseable {
	FramebufferDescription framebuffer();

	@Override
	void close();
}
