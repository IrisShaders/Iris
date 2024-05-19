package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector2i;

import java.util.function.Supplier;

public class Vector2IntegerJomlUniform extends Uniform {
	private final Supplier<Vector2i> value;
	private Vector2i cachedValue;

	Vector2IntegerJomlUniform(String name, int location, Supplier<Vector2i> value) {
		this(name, location, value, null);
	}

	Vector2IntegerJomlUniform(String name, int location, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

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

	@Override
	public UniformType getType() {
		return UniformType.VEC2I;
	}

	private void updateValue() {
		Vector2i newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2i(this.location, newValue.x, newValue.y);
		}
	}
}
