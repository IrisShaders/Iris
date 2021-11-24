package net.coderbot.iris.gl.uniform;

import org.lwjgl.opengl.GL21;

public class FloatUniform extends Uniform {
	private float cachedValue;
	private final FloatSupplier value;

	FloatUniform(int location, FloatSupplier value) {
		this(location, value, null);
	}

	FloatUniform(int location, FloatSupplier value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	private void updateValue() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			GL21.glUniform1f(location, newValue);
		}
	}
}
