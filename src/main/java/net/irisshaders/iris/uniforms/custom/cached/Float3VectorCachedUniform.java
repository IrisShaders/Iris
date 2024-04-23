package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.VectorType;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL21;

import java.util.function.Supplier;

public class Float3VectorCachedUniform extends VectorCachedUniform<Vector3f> {

	public Float3VectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Vector3f> supplier) {
		super(name, updateFrequency, new Vector3f(), supplier);
	}

	@Override
	protected void setFrom(Vector3f other) {
		this.cached.set(other);
	}

	@Override
	public void push(int location) {
		GL21.glUniform3f(location, this.cached.x, this.cached.y, this.cached.z);
	}

	@Override
	public VectorType getType() {
		return VectorType.VEC3;
	}
}
