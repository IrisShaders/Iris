package net.coderbot.iris.gl.uniform;

public abstract class Uniform {
	protected final int location;

	Uniform(int location) {
		this.location = location;
	}

	public abstract void update();
}
