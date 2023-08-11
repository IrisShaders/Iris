package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;

public abstract class Uniform {
	protected final int location;
	protected int bufferIndex;
	protected final ValueUpdateNotifier notifier;
	protected final String name;

	Uniform(String name, int location) {
		this(name, location, null);
	}

	Uniform(String name, int location, ValueUpdateNotifier notifier) {
		this.name = name;
		this.location = location;
		this.notifier = notifier;
	}

	public abstract void update();

	public final int getLocation() {
		return location;
	}

	public final ValueUpdateNotifier getNotifier() {
		return notifier;
	}

	public String getName() {
		return this.name;
	}

    public abstract int getByteSize();

	public abstract int getAlignment();

	public abstract void updateBuffer(long address);

	public void setBufferIndex(int bufferIndex) {
		this.bufferIndex = bufferIndex;
	}

	public abstract UniformType getType();
}
