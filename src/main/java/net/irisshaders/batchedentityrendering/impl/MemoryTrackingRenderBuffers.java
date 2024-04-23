package net.irisshaders.batchedentityrendering.impl;

public interface MemoryTrackingRenderBuffers {
	int getEntityBufferAllocatedSize();

	int getMiscBufferAllocatedSize();

	int getMaxBegins();

	void freeAndDeleteBuffers();
}
