package net.irisshaders.iris.backend;

import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import org.joml.Vector3i;

public interface ComputePipeline extends AutoCloseable {
	Vector3i localSize();

	void dispatch(Vector3i groups, FilledIndirectPointer indirect);

	@Override
	void close();
}
