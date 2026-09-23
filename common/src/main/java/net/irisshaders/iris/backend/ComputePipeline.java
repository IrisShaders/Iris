package net.irisshaders.iris.backend;

import org.joml.Vector3i;

public interface ComputePipeline extends AutoCloseable {
	Vector3i localSize();

	@Override
	void close();
}
