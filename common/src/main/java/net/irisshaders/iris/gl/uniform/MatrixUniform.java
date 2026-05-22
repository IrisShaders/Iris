package net.irisshaders.iris.gl.uniform;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46C;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class MatrixUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private final Supplier<Matrix4fc> value;
	private final Matrix4f cachedValue;

	MatrixUniform(int location, Supplier<Matrix4fc> value) {
		super(location);

		this.cachedValue = new Matrix4f();
		this.value = value;
	}

	MatrixUniform(int location, Supplier<Matrix4fc> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new Matrix4f();
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
		Matrix4fc newValue = value.get();

		if (!cachedValue.equals(newValue)) {
			cachedValue.set(newValue);

			cachedValue.get(buffer);
			buffer.rewind();

			GL46C.glUniformMatrix4fv(location, false, buffer);
		}
	}
}
