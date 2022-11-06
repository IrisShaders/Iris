package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

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
	public int getStandardOffsetBytes() {
		return 16;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		float[] value = this.value.get();
		MemoryUtil.memPutFloat(memoryOffset, value[0]);
		MemoryUtil.memPutFloat(memoryOffset + 4, value[1]);
		MemoryUtil.memPutFloat(memoryOffset + 8, value[2]);
		MemoryUtil.memPutFloat(memoryOffset + 12, value[3]);
	}

	@Override
	protected int getAlignment() {
		return 16;
	}

	@Override
	public String getTypeName() {
		return "vec4";
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
