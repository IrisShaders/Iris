package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector4f;

import java.util.function.Supplier;

public class Vector4Uniform extends Uniform {
	private final Vector4f cachedValue;
	private final Supplier<Vector4f> value;

	Vector4Uniform(String name, int location, Supplier<Vector4f> value) {
		this(name, location, value, null);
	}

	Vector4Uniform(String name, int location, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

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

	@Override
	public UniformType getType() {
		return UniformType.VEC4;
	}

	private void updateValue() {
		Vector4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z(), newValue.w());
			IrisRenderSystem.uniform4f(location, cachedValue.x(), cachedValue.y(), cachedValue.z(), cachedValue.w());
		}
	}
}
