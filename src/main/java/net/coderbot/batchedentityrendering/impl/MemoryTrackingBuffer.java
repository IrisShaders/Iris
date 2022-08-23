package net.coderbot.batchedentityrendering.impl;

public interface MemoryTrackingBuffer {
	int getAllocatedSize();
	int getUsedSize();
	void freeAndDeleteBuffer();
}
