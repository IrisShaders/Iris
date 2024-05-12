package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Supplier;

public class Vector3Uniform extends Uniform {
	private final Vector3f cachedValue;
	private final Supplier<Vector3f> value;

	Vector3Uniform(int location, Supplier<Vector3f> value) {
		super(location);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	Vector3Uniform(int location, Supplier<Vector3f> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	static Vector3Uniform converted(int location, Supplier<Vector3d> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(location, () -> {
			Vector3d updated = value.get();

			held.set((float) updated.x, (float) updated.y, (float) updated.z);

			return held;
		});
	}

	static Vector3Uniform truncated(int location, Supplier<Vector4f> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(location, () -> {
			Vector4f updated = value.get();

			held.set(updated.x(), updated.y(), updated.z());

			return held;
		});
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	private void updateValue() {
		Vector3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z());
			IrisRenderSystem.uniform3f(location, cachedValue.x(), cachedValue.y(), cachedValue.z());
		}
	}
}
