package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.VectorType;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class Float2VectorArrayCachedUniform extends VectorCachedUniform<Vector2f[]> {

	public Float2VectorArrayCachedUniform(String name, UniformUpdateFrequency updateFrequency, int count, Supplier<Vector2f[]> supplier) {
		super(name, updateFrequency, createArray(count), supplier);
	}

	private static Vector2f[] createArray(int count) {
		Vector2f[] array = new Vector2f[count];

		for (int i = 0; i < count; i++) {
			array[i] = new Vector2f();
		}

		return array;
	}

	@Override
	protected void setFrom(Vector2f[] other) {
		System.arraycopy(other, 0, this.cached, 0, this.cached.length);
	}

	@Override
	public void push(int location) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(cached.length * 2);

			int index = 0;
			for (int i = 0; i < cached.length; i++) {
				cached[i].get(index, buffer);
				index += 2;
			}

			GL46C.glUniform2fv(location, buffer);
		}
	}

	@Override
	public VectorType getType() {
		return VectorType.VEC2;
	}
}
