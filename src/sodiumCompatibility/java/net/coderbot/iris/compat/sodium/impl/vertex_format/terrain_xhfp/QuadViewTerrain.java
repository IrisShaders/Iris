package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import net.coderbot.iris.compat.sodium.impl.vertex_format.QuadView;

import java.nio.ByteBuffer;

public class QuadViewTerrain extends QuadView {
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

	@Override
	public float u(int index) {
		return normalizeVertexTextureShortAsFloat(buffer.getShort(writeOffset + 12 - STRIDE * (3 - index)));
	}

	@Override
	public float v(int index) {
		return normalizeVertexTextureShortAsFloat(buffer.getShort(writeOffset + 14 - STRIDE * (3 - index)));
	}

	private static float normalizeVertexTextureShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 32768.0f);
	}

	private static float normalizeVertexPositionShortAsFloat(short value) {
		return (value & 0xFFFF) * (1.0f / 65535.0f);
	}
}
