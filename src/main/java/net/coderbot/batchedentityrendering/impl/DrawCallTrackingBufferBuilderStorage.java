package net.coderbot.batchedentityrendering.impl;

public interface DrawCallTrackingBufferBuilderStorage {
	int getDrawCalls();
	int getRenderTypes();
	void resetDrawCounts();
}
