package net.irisshaders.iris.backend;

public interface PackResources extends AutoCloseable {
	void beginFrame();

	@Override
	void close();
}
