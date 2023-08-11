package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class Vector2IntegerJomlUniform extends Uniform {
	private Vector2i cachedValue;
	private final Supplier<Vector2i> value;

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
	public int getByteSize() {
		return 8;
	}

	@Override
	public int getAlignment() {
		return 8;
	}

	@Override
	public void updateBuffer(long address) {
		Vector2i newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;
			MemoryUtil.memPutInt(address + bufferIndex, newValue.x);
			MemoryUtil.memPutInt(address + bufferIndex + 4, newValue.y);
		}
	}

	@Override
	public UniformType getType() {
		return UniformType.IVEC2;
	}

	private void updateValue() {
		Vector2i newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2i(this.location, newValue.x, newValue.y);
		}
	}
}
