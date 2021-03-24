package net.coderbot.iris.vertices;

import java.nio.ByteBuffer;

public class QuadView {
	private ByteBuffer buffer;
	private int writeOffset;
	private int stride = 48;

	public void setup(ByteBuffer buffer, int writeOffset, int stride) {
		this.buffer = buffer;
		this.writeOffset = writeOffset;
		this.stride = stride;
	}

	public float x(int index) {
		return buffer.getFloat(writeOffset - stride * (4 - index));
	}

	public float y(int index) {
		return buffer.getFloat(writeOffset + 4 - stride * (4 - index));
	}

	public float z(int index) {
		return buffer.getFloat(writeOffset + 8 - stride * (4 - index));
	}

	public float u(int index) {
		return buffer.getFloat(writeOffset + 16 - stride * (4 - index));
	}

	public float v(int index) {
		return buffer.getFloat(writeOffset + 20 - stride * (4 - index));
	}
}
