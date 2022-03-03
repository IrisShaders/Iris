package net.coderbot.iris.compat.sodium.impl.vertex_format;

import java.nio.ByteBuffer;

public class QuadViewTerrain implements QuadView {
	public ByteBuffer buffer;
	public int writeOffset;
	private static final int STRIDE = 36;

	public float x(int index) {
		return normalizeVertexPositionShortAsFloat(buffer.getShort(writeOffset - STRIDE * (3 - index)));
	}

	public float y(int index) {
		return normalizeVertexPositionShortAsFloat(buffer.getShort(writeOffset + 2 - STRIDE * (3 - index)));
	}

	public float z(int index) {
		return normalizeVertexPositionShortAsFloat(buffer.getShort(writeOffset + 4 - STRIDE * (3 - index)));
	}

	// TODO: Verify that this works with the new changes to the CVF
	private static float normalizeVertexPositionShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 65535.0f);
	}
}
