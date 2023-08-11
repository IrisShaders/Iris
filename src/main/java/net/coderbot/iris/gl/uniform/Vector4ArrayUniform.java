package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.Arrays;
import java.util.function.Supplier;

public class Vector4ArrayUniform extends Uniform {
	private float[] cachedValue;
	private final Supplier<float[]> value;

	Vector4ArrayUniform(String name, int location, Supplier<float[]> value) {
		this(name, location, value, null);
	}

	Vector4ArrayUniform(String name, int location, Supplier<float[]> value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

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

	@Override
	public void updateBuffer(long address) {
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = newValue;

			MemoryUtil.memPutFloat(address + bufferIndex, newValue[0]);
			MemoryUtil.memPutFloat(address + bufferIndex + 4, newValue[1]);
			MemoryUtil.memPutFloat(address + bufferIndex + 8, newValue[2]);
			MemoryUtil.memPutFloat(address + bufferIndex + 12, newValue[3]);
		}
	}

	@Override
	public int getByteSize() {
		return 16;
	}

	@Override
	public int getAlignment() {
		return 16;
	}

	@Override
	public UniformType getType() {
		return UniformType.VEC4;
	}

	private void updateValue() {
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform4f(location, cachedValue[0], cachedValue[1], cachedValue[2], cachedValue[3]);
		}
	}
}
