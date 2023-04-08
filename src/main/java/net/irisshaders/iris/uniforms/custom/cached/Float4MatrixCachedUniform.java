package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.MatrixType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL21;

import java.util.function.Supplier;

public class Float4MatrixCachedUniform extends VectorCachedUniform<Matrix4f> {
	final private float[] buffer = new float[16];

	public Float4MatrixCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Matrix4f> supplier) {
		super(name, updateFrequency, new Matrix4f(), supplier);
	}

	@Override
	protected void setFrom(Matrix4f other) {
		this.cached.set(other);
	}

	@Override
	public void push(int location) {
		// `gets` the values from the matrix and put's them into a buffer
		this.cached.get(buffer);
		GL21.glUniformMatrix4fv(location, false, buffer);
	}

	@Override
	public MatrixType<Matrix4f> getType() {
		return MatrixType.MAT4;
	}
}
