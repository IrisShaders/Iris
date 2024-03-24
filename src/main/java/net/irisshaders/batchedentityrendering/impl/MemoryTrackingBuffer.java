package net.irisshaders.batchedentityrendering.impl;

public interface MemoryTrackingBuffer {
	int getAllocatedSize();

	int getUsedSize();

	void freeAndDeleteBuffer();
}
