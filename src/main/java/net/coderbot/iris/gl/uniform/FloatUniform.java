package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.system.MemoryUtil;

public class FloatUniform extends Uniform {
	private float cachedValue;
	private final FloatSupplier value;

	FloatUniform(int location, FloatSupplier value) {
		this(location, value, null);
	}

	FloatUniform(int location, FloatSupplier value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public int getStandardOffsetBytes() {
		return 4;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		MemoryUtil.memPutFloat(memoryOffset, value.getAsFloat());
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	@Override
	protected int getAlignment() {
		return 0;
	}

	@Override
	public String getTypeName() {
		return "float";
	}

	private void updateValue() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1f(location, newValue);
		}
	}
}
