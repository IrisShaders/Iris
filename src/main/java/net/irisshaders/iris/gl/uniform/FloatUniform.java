package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;

public class FloatUniform extends Uniform {
	private final FloatSupplier value;
	private float cachedValue;

	FloatUniform(String name, int location, FloatSupplier value) {
		this(name, location, value, null);
	}

	FloatUniform(String name, int location, FloatSupplier value, ValueUpdateNotifier notifier) {
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
		return UniformType.FLOAT;
	}

	private void updateValue() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1f(location, newValue);
		}
	}
}
