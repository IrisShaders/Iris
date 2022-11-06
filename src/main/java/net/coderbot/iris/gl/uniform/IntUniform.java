package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.IntSupplier;

public class IntUniform extends Uniform {
	private int cachedValue;
	private final IntSupplier value;

	IntUniform(int location, IntSupplier value) {
		this(location, value, null);
	}

	IntUniform(int location, IntSupplier value, ValueUpdateNotifier notifier) {
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
		MemoryUtil.memPutInt(memoryOffset, value.getAsInt());
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
		return "int";
	}

	private void updateValue() {
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			RenderSystem.glUniform1i(location, newValue);
		}
	}
}
