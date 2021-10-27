package kroppeb.stareval.function;

public abstract class AbstractTypedFunction implements TypedFunction {
	private final Type returnType;
	private final Type[] parameterType;

	public AbstractTypedFunction(Type returnType, Type[] parameterType) {
		this.returnType = returnType;
		this.parameterType = parameterType;
	}

	@Override
	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public Type[] getParameterTypes() {
		return this.parameterType;
	}
}
