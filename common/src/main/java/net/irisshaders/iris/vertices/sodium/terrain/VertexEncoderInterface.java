package net.irisshaders.iris.vertices.sodium.terrain;

public interface VertexEncoderInterface {
	void beginBlock(int blockId, byte isFluid, byte lightEmission, int x, int y, int z);

	void overrideBlock(int anInt);

	void restoreBlock();
}
