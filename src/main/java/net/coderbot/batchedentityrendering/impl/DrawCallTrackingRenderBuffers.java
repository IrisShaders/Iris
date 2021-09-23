package net.coderbot.batchedentityrendering.impl;

public interface DrawCallTrackingRenderBuffers {
	int getDrawCalls();
	int getRenderTypes();
	void resetDrawCounts();
}
