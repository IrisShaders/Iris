package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class Vector2IntegerJomlUniform extends Uniform {
	private Vector2i cachedValue;
	private final Supplier<Vector2i> value;

	Vector2IntegerJomlUniform(int location, Supplier<Vector2i> value) {
		this(location, value, null);
	}

	Vector2IntegerJomlUniform(int location, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public int getStandardOffsetBytes() {
		return 8;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		Vector2i value = this.value.get();
		MemoryUtil.memPutInt(memoryOffset, value.x());
		MemoryUtil.memPutInt(memoryOffset + 4, value.y());
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	private void updateValue() {
		Vector2i newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2i(this.location, newValue.x, newValue.y);
		}
	}

	@Override
	protected int getAlignment() {
		return 8;
	}

	@Override
	public String getTypeName() {
		return "ivec2";
	}
}
