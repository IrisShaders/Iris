package net.irisshaders.batchedentityrendering.impl;

public interface FlushableMultiBufferSource {
	void flushNonTranslucentContent();

	void flushTranslucentContent();
}
