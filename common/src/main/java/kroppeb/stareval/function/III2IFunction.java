package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

@FunctionalInterface
public interface III2IFunction extends TypedFunction {
	int eval(int a, int b, int c);

	@Override
	default void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
		params[0].evaluateTo(context, functionReturn);
		int a = functionReturn.intReturn;

		params[1].evaluateTo(context, functionReturn);
		int b = functionReturn.intReturn;

		params[2].evaluateTo(context, functionReturn);
		int c = functionReturn.intReturn;

		functionReturn.intReturn = this.eval(a, b, c);
	}

	@Override
	default Type getReturnType() {
		return Type.Int;
	}

	@Override
	default Parameter[] getParameters() {
		return new Parameter[]{Type.IntParameter, Type.IntParameter, Type.IntParameter};
	}
}
