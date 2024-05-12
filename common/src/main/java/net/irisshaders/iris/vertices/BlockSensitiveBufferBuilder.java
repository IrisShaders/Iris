package net.irisshaders.iris.vertices;

public interface BlockSensitiveBufferBuilder {
	void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ);

	void endBlock();
}
