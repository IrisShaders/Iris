package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.state.ValueUpdateNotifier;

public abstract class Uniform {
	protected final int location;
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
		return name;
	}

	public abstract UniformType getType();
}
