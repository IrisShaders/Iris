package net.irisshaders.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.VectorType;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class Int2VectorCachedUniform extends VectorCachedUniform<Vector2i> {

	public Int2VectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Vector2i> supplier) {
		super(name, updateFrequency, new Vector2i(), supplier);
	}

	@Override
	protected void setFrom(Vector2i other) {
		this.cached.set(other);
	}

	@Override
	public void push(int location) {
		GL21.glUniform2i(location, this.cached.x, this.cached.y);
	}

	@Override
	public void writeTo(FunctionReturn functionReturn) {
		functionReturn.objectReturn = this.cached;
	}

	@Override
	public long writeTo(long buffer) {
		MemoryUtil.memPutInt(buffer, cached.x);
		MemoryUtil.memPutInt(buffer + 4, cached.y);
		return buffer + 8;
	}

	@Override
	public VectorType getType() {
		return VectorType.I_VEC2;
	}
}
