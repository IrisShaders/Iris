package net.irisshaders.batchedentityrendering.impl;

public interface DrawCallTrackingRenderBuffers {
	int getDrawCalls();

	int getRenderTypes();

	void resetDrawCounts();
}
