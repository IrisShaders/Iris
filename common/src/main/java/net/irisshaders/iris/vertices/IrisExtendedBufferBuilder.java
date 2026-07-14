package net.irisshaders.iris.vertices;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;

public interface IrisExtendedBufferBuilder {
	VertexFormat iris$format();

	PrimitiveTopology iris$mode();

	boolean iris$extending();

	boolean iris$isTerrain();

	boolean iris$injectNormalAndUV1();

	int iris$vertexCount();

	void iris$incrementVertexCount();

	void iris$resetVertexCount();

	short iris$currentBlock();

	short iris$currentRenderType();

	int iris$currentLocalPosX();

	int iris$currentLocalPosY();

	int iris$currentLocalPosZ();
}
