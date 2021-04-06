package net.coderbot.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.opengl.GL21;

import java.util.function.BooleanSupplier;

public class BooleanCachedUniform extends CachedUniform {
	
	final private BooleanSupplier supplier;
	private boolean cached;
	
	public BooleanCachedUniform(String name, UniformUpdateFrequency updateFrequency, BooleanSupplier supplier) {
		super(name, updateFrequency);
		this.supplier = supplier;
	}
	
	@Override
	protected boolean doUpdate(){
		boolean prev = this.cached;
		this.cached = this.supplier.getAsBoolean();
		return prev == cached;
	}
	
	@Override
	protected void push(){
		GL21.glUniform1i(this.getLocation(), this.cached?1:0);
	}
	
	@Override
	public void writeTo(FunctionReturn functionReturn){
		functionReturn.booleanReturn = this.cached;
	}
	
	@Override
	public Type getType() {
		return Type.Boolean;
	}
}
