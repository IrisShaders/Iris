package kroppeb.stareval.function;

public abstract class AbstractTypedFunction implements TypedFunction {
	final private Type returnType;
	final private Type[] parameterType;
	
	public AbstractTypedFunction(Type returnType, Type[] parameterType) {
		this.returnType = returnType;
		this.parameterType = parameterType;
	}
	
	@Override
	public Type getReturnType() {
		return returnType;
	}
	
	@Override
	public Type[] getParameterTypes() {
		return parameterType;
	}
}
