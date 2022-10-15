package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.Arrays;
import java.util.function.Supplier;

public class Vector4ArrayUniform extends Uniform {
	private float[] cachedValue;
	private final Supplier<float[]> value;

	Vector4ArrayUniform(int location, Supplier<float[]> value) {
		this(location, value, null);
	}

	Vector4ArrayUniform(int location, Supplier<float[]> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new float[4];
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
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform4f(location, cachedValue[0], cachedValue[1], cachedValue[2], cachedValue[3]);
		}
	}
}
