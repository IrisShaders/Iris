package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

public class MatrixFromFloatArrayUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private float[] cachedValue;
	private final Supplier<float[]> value;

	MatrixFromFloatArrayUniform(int location, Supplier<float[]> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = Arrays.copyOf(newValue, 16);

			buffer.put(cachedValue);
			buffer.rewind();

			IrisRenderSystem.uniformMatrix4fv(location, false, buffer);
		}
	}
}
