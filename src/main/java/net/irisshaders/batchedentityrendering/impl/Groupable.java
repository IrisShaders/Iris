package net.irisshaders.batchedentityrendering.impl;

public interface Groupable {
	void startGroup();

	boolean maybeStartGroup();

	void endGroup();
}
