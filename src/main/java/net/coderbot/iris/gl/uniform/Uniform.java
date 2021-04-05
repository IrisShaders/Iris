package net.coderbot.iris.gl.uniform;

public abstract class Uniform {
	protected final int location;

	public Uniform(int location) {
		this.location = location;
	}

	public abstract void update();
}
