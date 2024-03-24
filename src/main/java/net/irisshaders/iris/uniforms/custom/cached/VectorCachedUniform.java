package net.irisshaders.iris.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class VectorCachedUniform<T> extends CachedUniform {

	final protected T cached;
	final private Supplier<T> supplier;

	public VectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, T cache, Supplier<@NotNull T> supplier) {
		super(name, updateFrequency);
		this.supplier = supplier;
		this.cached = cache;
	}

	abstract protected void setFrom(T other);

	@Override
	protected boolean doUpdate() {
		T other = this.supplier.get();
		if (other == null) {
			Iris.logger.warn("Cached Uniform supplier gave null back");
			return false;
		}
		if (!this.cached.equals(other)) {
			this.setFrom(other);
			return true;
		} else return false;
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
