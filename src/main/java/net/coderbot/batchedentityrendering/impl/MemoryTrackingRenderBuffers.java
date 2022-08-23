package net.coderbot.batchedentityrendering.impl;

public interface MemoryTrackingRenderBuffers {
    int getEntityBufferAllocatedSize();
    int getMiscBufferAllocatedSize();
    int getMaxBegins();
	void freeAndDeleteBuffers();
}
