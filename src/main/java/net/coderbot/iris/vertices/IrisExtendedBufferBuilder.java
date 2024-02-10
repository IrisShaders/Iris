package net.coderbot.iris.vertices;

import com.mojang.blaze3d.vertex.VertexFormat;

public interface IrisExtendedBufferBuilder {
	VertexFormat iris$format();

	VertexFormat.Mode iris$mode();

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
