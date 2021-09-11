package net.coderbot.batchedentityrendering.impl;

public interface FlushableVertexConsumerProvider {
	void flushNonTranslucentContent();
	void flushTranslucentContent();
}
