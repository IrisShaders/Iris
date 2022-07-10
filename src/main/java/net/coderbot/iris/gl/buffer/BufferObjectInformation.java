package net.coderbot.iris.gl.buffer;

public class BufferObjectInformation implements Comparable<BufferObjectInformation> {
	private final int index;
	private final long size;
	private final boolean clear;

	public BufferObjectInformation(int index, long size, boolean clear) {
		this.index = index;
		this.size = size;
		this.clear = clear;
	}

	public int getIndex() {
		return index;
	}

	public long getSize() {
		return size;
	}

	public boolean shouldClear() {
		return clear;
	}

	@Override
	public int compareTo(BufferObjectInformation o) {
		if (this.getIndex() > o.getIndex()) {
			return 1;
		} else if (this.getIndex() < o.getIndex()) {
			return -1;
		}
		return 0;
	}
}
