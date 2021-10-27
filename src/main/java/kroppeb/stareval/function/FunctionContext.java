package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

@FunctionalInterface
public interface FunctionContext {
	Expression getVariable(String name);
}
