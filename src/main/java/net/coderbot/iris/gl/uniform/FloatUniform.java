package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.system.MemoryUtil;

public class FloatUniform extends Uniform {
	private float cachedValue;
	private final FloatSupplier value;

	FloatUniform(String name, int location, FloatSupplier value) {
		this(name, location, value, null);
	}

	FloatUniform(String name, int location, FloatSupplier value, ValueUpdateNotifier notifier) {
		super(name, location, notifier);

		this.cachedValue = 0;
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
		return 4;
	}

	@Override
	public int getAlignment() {
		return 4;
	}

	@Override
	public UniformType getType() {
		return UniformType.FLOAT;
	}

	@Override
	public void updateBuffer(long address) {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			MemoryUtil.memPutFloat(address + bufferIndex, newValue);
		}
	}

	private void updateValue() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1f(location, newValue);
		}
	}
}
