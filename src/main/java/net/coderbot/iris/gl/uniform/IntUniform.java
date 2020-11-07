package net.coderbot.iris.gl.uniform;

import java.util.function.IntSupplier;

import org.lwjgl.opengl.GL21;

public class IntUniform extends Uniform {
	private int cachedValue;
	private final IntSupplier value;

	IntUniform(int location, IntSupplier value) {
		super(location);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public void update() {
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			GL21.glUniform1i(location, newValue);
		}
	}
}
