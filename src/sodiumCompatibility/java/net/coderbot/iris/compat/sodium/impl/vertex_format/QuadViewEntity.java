package net.coderbot.iris.compat.sodium.impl.vertex_format;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class QuadViewEntity implements QuadView {
	public ByteBuffer buffer;
	public int writeOffset;
	public long writePointer;
	private final int stride;
	private final boolean unsafe;

	public QuadViewEntity(int stride, boolean unsafe) {
		this.stride = stride;
		this.unsafe = unsafe;
	}

	public float x(int index) {
		if (unsafe) {
			return MemoryUtil.memGetFloat(writePointer - stride * (4 - index));
		} else {
			return buffer.getFloat(writeOffset - stride * (4 - index));
		}
	}

	public float y(int index) {
		if (unsafe) {
			return MemoryUtil.memGetFloat(writePointer + 4 - stride * (4 - index));
		} else {
			return buffer.getFloat(writeOffset + 4 - stride * (4 - index));
		}
	}

	public float z(int index) {
		if (unsafe) {
			return MemoryUtil.memGetFloat(writePointer + 8 - stride * (4 - index));
		} else {
			return buffer.getFloat(writeOffset + 8 - stride * (4 - index));
		}
	}
}
