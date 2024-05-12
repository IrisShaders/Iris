package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

@FunctionalInterface
public interface II2BFunction extends TypedFunction {
	boolean eval(int a, int b);

	@Override
	default void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
		params[0].evaluateTo(context, functionReturn);
		int a = functionReturn.intReturn;

		params[1].evaluateTo(context, functionReturn);
		int b = functionReturn.intReturn;

		functionReturn.booleanReturn = this.eval(a, b);
	}

	@Override
	default Type getReturnType() {
		return Type.Boolean;
	}

	@Override
	default Parameter[] getParameters() {
		return new Parameter[]{Type.IntParameter, Type.IntParameter};
	}
}
