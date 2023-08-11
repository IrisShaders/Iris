package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class Vector2Uniform extends Uniform {
	private Vector2f cachedValue;
	private final Supplier<Vector2f> value;

	Vector2Uniform(String name, int location, Supplier<Vector2f> value) {
		super(name, location);

		this.cachedValue = null;
		this.value = value;
	}

	Vector2Uniform(String name, int location, Supplier<Vector2f> value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

		this.cachedValue = null;
		this.value = value;

	}

	@Override
	public void updateBuffer(long address) {
		Vector2f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;
			MemoryUtil.memPutFloat(address + bufferIndex, newValue.x);
			MemoryUtil.memPutFloat(address + bufferIndex + 4, newValue.y);
		}
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	@Override
	public int getByteSize() {
		return 8;
	}

	@Override
	public int getAlignment() {
		return 8;
	}

	@Override
	public UniformType getType() {
		return UniformType.VEC3;
	}

	private void updateValue() {
		Vector2f newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2f(this.location, newValue.x, newValue.y);
		}
	}
}
