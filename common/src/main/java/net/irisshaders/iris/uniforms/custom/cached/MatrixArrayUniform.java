package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.MatrixType;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

import java.util.function.Supplier;

public class MatrixArrayUniform extends VectorCachedUniform<Matrix4fc[]> {
	public MatrixArrayUniform(String name, int count, UniformUpdateFrequency updateFrequency, Supplier<Matrix4fc[]> supplier) {
		super(name, updateFrequency, createMatrixArray(count), supplier);
	}

	private static Matrix4fc[] createMatrixArray(int count) {
		Matrix4fc[] matrices = new Matrix4fc[count];

		for (int i = 0; i < count; i++) {
			matrices[i] = new Matrix4f();
		}

		return matrices;
	}

	@Override
	protected void setFrom(Matrix4fc[] other) {
		for (int i = 0; i < other.length; i++) {
			((Matrix4f) this.cached[i]).set(other[i]);
		}
	}

	@Override
	public void push(int location) {
		// `gets` the values from the matrix and put's them into a buffer
		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(64 * cached.length);

			int offset = 0;

			for (Matrix4fc matrix : cached) {
				matrix.getToAddress(buffer + offset);
				offset += 64;
			}

			GL46C.nglUniformMatrix4fv(location, cached.length, false, buffer);
		}
	}

	@Override
	public MatrixType<Matrix4f> getType() {
		return MatrixType.MAT4;
	}
}
