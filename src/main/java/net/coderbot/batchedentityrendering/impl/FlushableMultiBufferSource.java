package net.coderbot.batchedentityrendering.impl;

public interface FlushableMultiBufferSource {
	void flushNonTranslucentContent();
	void flushTranslucentContent();
}
