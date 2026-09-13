package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.MatrixType;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.function.Supplier;

public class Float3MatrixCachedUniform extends VectorCachedUniform<Matrix3fc> {
	final private float[] buffer = new float[9];

	public Float3MatrixCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Matrix3fc> supplier) {
		super(name, updateFrequency, new Matrix3f(), supplier);
	}

	@Override
	protected void setFrom(Matrix3fc other) {
		((Matrix3f) this.cached).set(other);
	}

	@Override
	public void push(int location) {
		// `gets` the values from the matrix and put's them into a buffer
		this.cached.get(buffer);
		IrisRenderSystem.uniformMatrix3fv(location, false, buffer);
	}

	@Override
	public MatrixType<Matrix3f> getType() {
		return MatrixType.MAT3;
	}
}
