package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.system.MemoryUtil;

import java.util.function.IntSupplier;

public class IntUniform extends Uniform {
	private int cachedValue;
	private final IntSupplier value;

	IntUniform(String name, int location, IntSupplier value) {
		this(name, location, value, null);
	}

	IntUniform(String name, int location, IntSupplier value, ValueUpdateNotifier notifier) {
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
	public void updateBuffer(long address) {
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			MemoryUtil.memPutInt(address + bufferIndex, newValue);
		}
	}

	@Override
	public UniformType getType() {
		return UniformType.INT;
	}

	private void updateValue() {
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			RenderSystem.glUniform1i(location, newValue);
		}
	}
}
