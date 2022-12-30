package net.coderbot.iris.uniforms.custom.cached;

import com.mojang.math.Vector3f;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.VectorType;
import org.lwjgl.opengl.GL21;

import java.util.function.Supplier;

public class Float3VanillaVectorCachedUniform extends VectorCachedUniform<Vector3f> {

	public Float3VanillaVectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Vector3f> supplier) {
		super(name, updateFrequency, new Vector3f(), supplier);
	}

	@Override
	protected void setFrom(Vector3f other) {
		this.cached.set(other.x(), other.y(), other.z());
	}

	@Override
	public void push(int location) {
		GL21.glUniform3f(location, this.cached.x(), this.cached.y(), this.cached.z());
	}

	@Override
	public VectorType getType() {
		return VectorType.VEC3;
	}
}
