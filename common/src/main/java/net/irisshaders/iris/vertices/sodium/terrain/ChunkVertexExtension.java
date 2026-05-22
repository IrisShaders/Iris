package net.irisshaders.iris.vertices.sodium.terrain;

public interface ChunkVertexExtension {
	void iris$setData(byte blockEmission, byte renderType, int blockId, int localX, int localY, int localZ);

	void iris$ignoresMidBlock(boolean setIgnore);

	void iris$copyData(ChunkVertexExtension dest);

	int getLocalPosX();

	int getLocalPosY();

	int getLocalPosZ();

	int getBlockId();

	byte getRenderType();

	byte getBlockEmission();

	boolean ignoreMidBlock();
}
