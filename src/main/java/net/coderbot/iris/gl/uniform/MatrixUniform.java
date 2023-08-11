package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class MatrixUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	MatrixUniform(String name, int location, Supplier<Matrix4f> value) {
		super(name, location);

		this.cachedValue = null;
		this.value = value;
	}

	MatrixUniform(String name, int location, Supplier<Matrix4f> value, ValueUpdateNotifier notifier) {
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
		return 64;
	}

	@Override
	public int getAlignment() {
		return 16;
	}

	@Override
	public void updateBuffer(long address) {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = new Matrix4f(newValue);

			newValue.getToAddress(address + bufferIndex);
		}
	}

	@Override
	public UniformType getType() {
		return UniformType.MAT4;
	}

	public void updateValue() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = new Matrix4f(newValue);

			cachedValue.get(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix4(location, false, buffer);
		}
	}
}
