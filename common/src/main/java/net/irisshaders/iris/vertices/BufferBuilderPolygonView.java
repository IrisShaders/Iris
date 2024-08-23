package net.irisshaders.iris.vertices;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

public class BufferBuilderPolygonView implements QuadView {
	private long[] writePointers;

	public void setup(long[] writePointers, int stride, int vertexAmount) {
		this.writePointers = writePointers;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(writePointers[index]);
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(writePointers[index] + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(writePointers[index] + 8);
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(writePointers[index] + 16);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(writePointers[index] + 20);
	}
}
