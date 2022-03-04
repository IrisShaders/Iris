package net.coderbot.iris.vertices;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class QuadView {
	private ByteBuffer buffer;
	private long writePointer;
	private int stride = 48;
	private boolean unsafe;

	public void setup(ByteBuffer buffer, int writeOffset, int stride) {
		this.buffer = buffer;
		this.writePointer = writeOffset;
		this.stride = stride;
		this.unsafe = false;
	}

	public void setup(long writePointer, int stride) {
		this.writePointer = writePointer;
		this.stride = stride;
		this.unsafe = true;
	}

	public float x(int index) {
		return getFloat(writePointer - stride * (4L - index));
	}

	public float y(int index) {
		return getFloat(writePointer + 4 - stride * (4L - index));
	}

	public float z(int index) {
		return getFloat(writePointer + 8 - stride * (4L - index));
	}

	public float u(int index) {
		return getFloat(writePointer + 16 - stride * (4L - index));
	}

	public float v(int index) {
		return getFloat(writePointer + 20 - stride * (4L - index));
	}

	private float getFloat(long writePointer) {
		if (unsafe) {
			return MemoryUtil.memGetFloat(writePointer);
		} else {
			return buffer.getFloat((int) writePointer);
		}
	}
 }
