package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

public interface TypedFunction {
	
	Type getReturnType();

	Type[] getParameterTypes();

	void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn);
	
	default boolean isPure(){
		return true;
	}
}

