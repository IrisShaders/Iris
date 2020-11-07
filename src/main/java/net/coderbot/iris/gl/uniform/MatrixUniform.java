package net.coderbot.iris.gl.uniform;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL21;

import net.minecraft.util.math.Matrix4f;

public class MatrixUniform extends Uniform {
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	MatrixUniform(int location, Supplier<Matrix4f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue.copy();

			cachedValue.writeToBuffer(buffer);
			buffer.rewind();

			GL21.glUniformMatrix4fv(location, false, buffer);
		}
	}
}
