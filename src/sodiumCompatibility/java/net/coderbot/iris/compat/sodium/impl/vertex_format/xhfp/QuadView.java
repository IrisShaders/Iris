package net.coderbot.iris.compat.sodium.impl.vertex_format.xhfp;

import java.nio.ByteBuffer;

public class QuadView {
	ByteBuffer buffer;
	int writeOffset;
	private static final int STRIDE = XHFPModelVertexType.STRIDE;

	float x(int index) {
		return XHFPModelVertexType.decodePosition(buffer.getShort(writeOffset + 0 - STRIDE * (3 - index)));
	}

	float y(int index) {
		return XHFPModelVertexType.decodePosition(buffer.getShort(writeOffset + 2 - STRIDE * (3 - index)));
	}

	float z(int index) {
		return XHFPModelVertexType.decodePosition(buffer.getShort(writeOffset + 4 - STRIDE * (3 - index)));
	}
}
