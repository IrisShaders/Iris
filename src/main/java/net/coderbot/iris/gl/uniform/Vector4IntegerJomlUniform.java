package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector4i;

import java.util.function.Supplier;

public class Vector4IntegerJomlUniform extends Uniform {
	private Vector4i cachedValue;
	private final Supplier<Vector4i> value;

	Vector4IntegerJomlUniform(int location, Supplier<Vector4i> value) {
		this(location, value, null);
	}

	Vector4IntegerJomlUniform(int location, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = null;
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
		Vector4i newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform4i(this.location, newValue.x, newValue.y, newValue.z, newValue.w);
		}
	}
}
