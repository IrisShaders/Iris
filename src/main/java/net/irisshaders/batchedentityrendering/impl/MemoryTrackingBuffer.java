package net.irisshaders.batchedentityrendering.impl;

public interface MemoryTrackingBuffer {
	long getAllocatedSize();

	long getUsedSize();

	void freeAndDeleteBuffer();
}
