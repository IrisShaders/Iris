package net.irisshaders.iris.compat.sodium.impl.block_context;

public interface ContextAwareVertexWriter {
	void iris$setContextHolder(BlockContextHolder holder);

	void flipUpcomingQuadNormal();
}
