package net.coderbot.iris.vertices;

public interface BlockSensitiveBufferBuilder {
	void beginBlock(short block, short renderType);
	void endBlock();
}
