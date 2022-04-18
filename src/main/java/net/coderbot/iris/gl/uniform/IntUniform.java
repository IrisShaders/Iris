package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;

import java.util.function.IntSupplier;

public class IntUniform extends Uniform {
	private int cachedValue;
	private final IntSupplier value;

	IntUniform(int location, IntSupplier value) {
		this(location, value, null);
	}

	IntUniform(int location, IntSupplier value, ValueUpdateNotifier notifier) {
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
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1i(location, newValue);
		}
	}
}
