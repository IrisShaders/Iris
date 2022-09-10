package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

@FunctionalInterface
public interface BB2BFunction extends TypedFunction {
	boolean eval(boolean a, boolean b);

	@Override
	default void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
		params[0].evaluateTo(context, functionReturn);
		boolean a = functionReturn.booleanReturn;

		params[1].evaluateTo(context, functionReturn);
		boolean b = functionReturn.booleanReturn;

		functionReturn.booleanReturn = this.eval(a, b);
	}

	@Override
	default Type getReturnType() {
		return Type.Boolean;
	}

	@Override
	default Parameter[] getParameters() {
		return new Parameter[]{Type.BooleanParameter, Type.BooleanParameter};
	}
}
