package kroppeb.stareval.expression;

import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.TypedFunction;

public class CallExpression implements Expression{
	final private TypedFunction function;
	final private Expression[] arguments;
	
	public CallExpression(TypedFunction function, Expression[] arguments) {
		this.function = function;
		this.arguments = arguments;
	}
	
	@Override
	public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
		function.evaluateTo(arguments, context, functionReturn);
	}
}
