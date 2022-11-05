package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector2f;

import java.util.function.Supplier;

public class Vector2Uniform extends Uniform {
	private Vector2f cachedValue;
	private final Supplier<Vector2f> value;

	Vector2Uniform(int location, Supplier<Vector2f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	Vector2Uniform(int location, Supplier<Vector2f> value, ValueUpdateNotifier notifier) {
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
		Vector2f newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2f(this.location, newValue.x, newValue.y);
		}
	}
}
