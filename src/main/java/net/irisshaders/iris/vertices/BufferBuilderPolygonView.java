package net.irisshaders.iris.vertices;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferBuilderPolygonView implements QuadView {
	private long writePointer;
	private int stride = 48;
	private int vertexAmount;

	public void setup(long writePointer, int stride, int vertexAmount) {
		this.writePointer = writePointer;
		this.stride = stride;
		this.vertexAmount = vertexAmount;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(writePointer - stride * (vertexAmount - index));
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(writePointer + 4 - stride * (vertexAmount - index));
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(writePointer + 8 - stride * (vertexAmount - index));
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(writePointer + 16 - stride * (vertexAmount - index));
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(writePointer + 20 - stride * (vertexAmount - index));
	}
}
