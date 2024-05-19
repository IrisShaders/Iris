package net.irisshaders.iris.uniforms.custom.cached;

import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.VectorType;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class Int3VectorCachedUniform extends VectorCachedUniform<Vector3i> {

	public Int3VectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Vector3i> supplier) {
		super(name, updateFrequency, new Vector3i(), supplier);
	}

	@Override
	protected void setFrom(Vector3i other) {
		this.cached.set(other);
	}

	@Override
	public void push(int location) {
		GL21.glUniform3i(location, this.cached.x, this.cached.y, this.cached.z);
	}

	@Override
	public long writeTo(long buffer) {
		MemoryUtil.memPutInt(buffer, cached.x);
		MemoryUtil.memPutInt(buffer + 4L, cached.y);
		MemoryUtil.memPutInt(buffer + 8L, cached.z);
		return buffer + 12L;
	}

	@Override
	public VectorType getType() {
		return VectorType.I_VEC3;
	}
}
