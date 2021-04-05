package kroppeb.stareval.expression;

import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;

public interface Expression {
	void evaluateTo(FunctionContext context, FunctionReturn functionReturn);
}
