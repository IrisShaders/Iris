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

	MatrixUniform(int location, Supplier<Matrix4f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	MatrixUniform(int location, Supplier<Matrix4f> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

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
