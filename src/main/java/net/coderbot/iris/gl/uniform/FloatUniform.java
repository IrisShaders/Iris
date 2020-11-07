package net.coderbot.iris.gl.uniform;

import org.lwjgl.opengl.GL21;

public class FloatUniform extends Uniform {
	private float cachedValue;
	private final FloatSupplier value;

	FloatUniform(int location, FloatSupplier value) {
		super(location);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public void update() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			GL21.glUniform1f(location, newValue);
		}
	}
}
