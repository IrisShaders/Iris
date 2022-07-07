package net.coderbot.iris.gl.buffer;

public class BufferMapping {
	private final int index;
	private final int location;
	private final BufferType type;

	public BufferMapping(int index, int location, BufferType type) {
		this.index = index;
		this.location = location;
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public int getLocation() {
		return location;
	}

	public BufferType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Buffer object " + index + " at location " + location + " of type " + type;
	}
}
