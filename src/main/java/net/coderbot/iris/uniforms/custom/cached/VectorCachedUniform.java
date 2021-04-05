package net.coderbot.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class VectorCachedUniform<T> extends CachedUniform {
	
	final private Supplier<T> supplier;
	protected T cached;
	
	public VectorCachedUniform(UniformUpdateFrequency updateFrequency, Supplier<@NotNull T> supplier) {
		super(updateFrequency);
		this.supplier = supplier;
	}
	
	abstract protected void setFrom(T other);
	
	@Override
	protected boolean doUpdate(){
		T other = this.supplier.get();
		if(other == null){
			Iris.logger.warn("Cached Uniform supplier gave null back");
			return false;
		}
		if (this.cached != null && this.cached.equals(other)){
			this.setFrom(other);
			return true;
		}
		else return false;
	}
	
	@Override
	public void writeTo(FunctionReturn functionReturn){
		functionReturn.objectReturn = this.cached;
	}
	
	@Override
	public Type getType() {
		return Type.Float;
	}
}
