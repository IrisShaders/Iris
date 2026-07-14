package net.irisshaders.iris.vertices;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;

public interface ExtendingBufferBuilder {
	void iris$beginWithoutExtending(PrimitiveTopology drawMode, VertexFormat vertexFormat);
}
