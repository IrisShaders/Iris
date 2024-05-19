package net.irisshaders.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;

import java.util.function.IntSupplier;

public class IntUniform extends Uniform {
	private final IntSupplier value;
	private int cachedValue;

	IntUniform(String name, int location, IntSupplier value) {
		this(name, location, value, null);
	}

	IntUniform(String name, int location, IntSupplier value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

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

	@Override
	public UniformType getType() {
		return UniformType.INT;
	}

	private void updateValue() {
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			RenderSystem.glUniform1i(location, newValue);
		}
	}
}
