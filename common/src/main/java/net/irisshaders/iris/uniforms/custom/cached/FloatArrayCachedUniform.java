package net.irisshaders.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.opengl.GL21;

import java.util.function.Supplier;

public class FloatArrayCachedUniform extends CachedUniform {

	final private Supplier<float[]> supplier;
	private float[] cached;

	public FloatArrayCachedUniform(String name, UniformUpdateFrequency updateFrequency, int count, Supplier<float[]> supplier) {
		super(name, updateFrequency);
		this.supplier = supplier;
		this.cached = new float[count];
	}

	@Override
	protected boolean doUpdate() {
		float[] prev = this.cached;
		System.arraycopy(this.supplier.get(), 0, this.cached, 0, this.cached.length);
		return prev != cached;
	}

	@Override
	public void push(int location) {
		GL21.glUniform1fv(location, this.cached);
	}

	@Override
	public void writeTo(FunctionReturn functionReturn) {
		functionReturn.objectReturn = this.cached;
	}

	@Override
	public Type getType() {
		return Type.Float;
	}
}
