package net.coderbot.iris.gl.uniform;

import com.mojang.math.Vector4f;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL21;

public class Vector4Uniform extends Uniform {
	private final Vector4f cachedValue;
	private final Supplier<Vector4f> value;

	Vector4Uniform(int location, Supplier<Vector4f> value) {
		this(location, value, null);
	}

	Vector4Uniform(int location, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new Vector4f();
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
		Vector4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z(), newValue.w());
			GL21.glUniform4f(location, cachedValue.x(), cachedValue.y(), cachedValue.z(), cachedValue.w());
		}
	}
}
