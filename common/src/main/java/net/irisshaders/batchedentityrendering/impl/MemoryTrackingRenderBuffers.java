package net.irisshaders.batchedentityrendering.impl;

public interface MemoryTrackingRenderBuffers {
	long getEntityBufferAllocatedSize();

	long getMiscBufferAllocatedSize();

	int getMaxBegins();

	void freeAndDeleteBuffers();
}
