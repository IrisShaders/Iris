package net.coderbot.iris.uniforms.custom.cached;

import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.MatrixType;
import net.coderbot.iris.vendored.joml.Matrix4f;

import java.util.function.Supplier;

public class Float4MatrixCachedUniform extends VectorCachedUniform<Matrix4f> {
	
	public Float4MatrixCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Matrix4f> supplier) {
		super(name, updateFrequency, new Matrix4f(), supplier);
	}
	
	@Override
	protected void setFrom(Matrix4f other) {
		this.cached.set(other);
	}
	
	@Override
	protected void push() {
		throw new RuntimeException("not supported yet");
	}
	
	@Override
	public MatrixType<Matrix4f> getType() {
		return MatrixType.MAT4;
	}
}
