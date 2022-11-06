package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;

public abstract class Uniform {
	protected final int location;
	protected final ValueUpdateNotifier notifier;

	Uniform(int location) {
		this(location, null);
	}

	Uniform(int location, ValueUpdateNotifier notifier) {
		this.location = location;
		this.notifier = notifier;
	}

	public abstract int getStandardOffsetBytes();

	public abstract void putInBuffer(long memoryOffset);

	public abstract void update();

	public final int getLocation() {
		return location;
	}

	public final ValueUpdateNotifier getNotifier() {
		return notifier;
	}

	public int align(int bufferSize) {
		int alignment = getAlignment();
		if (alignment == 0) return bufferSize;
		return (((bufferSize - 1) + alignment) & -alignment);
	}

	protected abstract int getAlignment();

    public abstract String getTypeName();
}
