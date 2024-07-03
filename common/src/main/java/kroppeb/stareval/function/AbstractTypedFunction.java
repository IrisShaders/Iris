package kroppeb.stareval.function;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractTypedFunction implements TypedFunction {
	private final Type returnType;
	private final Parameter[] parameters;
	private final int priority;
	private final boolean isPure;

	public AbstractTypedFunction(Type returnType, Parameter[] parameters, int priority, boolean isPure) {
		this.returnType = returnType;
		this.parameters = parameters;
		this.priority = priority;
		this.isPure = isPure;
	}

	public AbstractTypedFunction(Type returnType, Type[] parameterType) {
		this.returnType = returnType;
		this.parameters = Arrays.stream(parameterType).map(Parameter::new).toArray(Parameter[]::new);
		this.priority = 0;
		this.isPure = true;
	}

	@Override
	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public Parameter[] getParameters() {
		return this.parameters;
	}

	@Override
	public boolean isPure() {
		return this.isPure;
	}

	@Override
	public int priority() {
		return this.priority;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractTypedFunction func) {
			return Objects.equals(returnType, func.returnType) &&
				Arrays.equals(parameters, func.parameters) &&
			priority == func.priority &&
			isPure == func.isPure;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(returnType, Arrays.hashCode(parameters), priority, isPure);
	}

	@Override
	public String toString() {
		return TypedFunction.format(this, "unknown");
	}
}
