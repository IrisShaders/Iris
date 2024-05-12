package net.irisshaders.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.opengl.GL21;

import java.util.function.IntSupplier;

public class IntCachedUniform extends CachedUniform {

	final private IntSupplier supplier;
	private int cached;

	public IntCachedUniform(String name, UniformUpdateFrequency updateFrequency, IntSupplier supplier) {
		super(name, updateFrequency);
		this.supplier = supplier;
	}

	@Override
	protected boolean doUpdate() {
		int prev = this.cached;
		this.cached = this.supplier.getAsInt();
		return prev != cached;
	}

	@Override
	public void push(int location) {
		GL21.glUniform1i(location, this.cached);
	}

	@Override
	public void writeTo(FunctionReturn functionReturn) {
		functionReturn.intReturn = this.cached;
	}

	@Override
	public Type getType() {
		return Type.Int;
	}
}
