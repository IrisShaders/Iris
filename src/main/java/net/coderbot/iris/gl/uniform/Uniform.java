package net.coderbot.iris.gl.uniform;

public abstract class Uniform {
	protected final int location;
	protected final ValueUpdateNotifier notifier;

	public Uniform(int location) {
		this(location, null);
	}

	public Uniform(int location, ValueUpdateNotifier notifier) {
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
}
