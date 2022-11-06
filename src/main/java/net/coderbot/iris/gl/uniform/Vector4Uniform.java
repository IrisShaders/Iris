package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

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
			IrisRenderSystem.uniform4f(location, cachedValue.x(), cachedValue.y(), cachedValue.z(), cachedValue.w());
		}
	}

	@Override
	public int getStandardOffsetBytes() {
		return 16;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		Vector4f value = this.value.get();
		MemoryUtil.memPutFloat(memoryOffset, value.x);
		MemoryUtil.memPutFloat(memoryOffset + 4, value.y);
		MemoryUtil.memPutFloat(memoryOffset + 8, value.z);
		MemoryUtil.memPutFloat(memoryOffset + 12, value.w);
	}

	@Override
	protected int getAlignment() {
		return 16;
	}

	@Override
	public String getTypeName() {
		return "vec4";
	}
}
