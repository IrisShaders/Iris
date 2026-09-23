package net.irisshaders.iris.vertices.sodium;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

public class QuadViewEntity implements QuadView {
	private long writePointer;
	private int stride;

	public void setup(long writePointer, int stride) {
		this.writePointer = writePointer;
		this.stride = stride;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(writePointer - stride * (3L - index));
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(writePointer + 4 - stride * (3L - index));
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(writePointer + 8 - stride * (3L - index));
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(writePointer + 16 - stride * (3L - index));
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(writePointer + 20 - stride * (3L - index));
	}
}
