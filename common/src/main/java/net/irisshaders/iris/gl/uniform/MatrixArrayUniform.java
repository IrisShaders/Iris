package net.irisshaders.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

public class MatrixArrayUniform extends Uniform {
	private final Supplier<Matrix4fc[]> value;
	private final int count;
	private Matrix4fc[] cachedValue;

	MatrixArrayUniform(int location, int count, Supplier<Matrix4fc[]> value) {
		super(location);

		this.count = count;
		this.value = value;
	}

	MatrixArrayUniform(int location, int count, Supplier<Matrix4fc[]> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.count = count;
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
		Matrix4fc[] values = value.get();

		if (Arrays.equals(cachedValue, values)) {
			return;
		} else {
			System.arraycopy(values, 0, cachedValue, 0, values.length);
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(64 * count);

			int offset = 0;

			for (Matrix4fc matrix : values) {
				matrix.getToAddress(buffer + offset);
				offset += 64;
			}

			GL46C.nglUniformMatrix4fv(location, count, false, buffer);
		}
	}
}
