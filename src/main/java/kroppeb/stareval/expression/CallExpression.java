package kroppeb.stareval.expression;

import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.TypedFunction;

import java.util.Collection;

public class CallExpression implements Expression {
	private final TypedFunction function;
	private final Expression[] arguments;

	public CallExpression(TypedFunction function, Expression[] arguments) {
		this.function = function;
		this.arguments = arguments;
	}

	@Override
	public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
		this.function.evaluateTo(this.arguments, context, functionReturn);
	}

	@Override
	public void listVariables(Collection<? super VariableExpression> variables) {
		for (Expression argument : this.arguments) {
			argument.listVariables(variables);
		}
	}

	private boolean isConstant() {
		for (Expression i : arguments) {
			if (!(i instanceof ConstantExpression)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Expression partialEval(FunctionContext context, FunctionReturn functionReturn) {
		// the dirty way would be this
		/*
			try{
				this.evaluateTo(context, functionReturn);
				return this.function.getReturnType().createConstant(functionReturn);
			}catch(Exception e){
				return this;
			}
		 */

		if (this.function.isPure() && isConstant()) {
			this.evaluateTo(context, functionReturn);
			return function.getReturnType().createConstant(functionReturn);
		}

		Expression[] partialEvaluatedParams = new Expression[this.arguments.length];
		boolean allFullySimplified = true;
		boolean noneSimplified = true;
		for (int i = 0; i < this.arguments.length; i++) {
			Expression simplified = this.arguments[i].partialEval(context, functionReturn);
			if (simplified instanceof ConstantExpression) {
				noneSimplified = false;
			} else {
				allFullySimplified = false;
				if (simplified != this.arguments[i]) {
					noneSimplified = false;
				}
			}
			partialEvaluatedParams[i] = simplified;
		}

		if (this.function.isPure() && allFullySimplified) {
			this.function.evaluateTo(partialEvaluatedParams, context, functionReturn);
			return this.function.getReturnType().createConstant(functionReturn);
		}

		if (noneSimplified) {
			return this;
		}

		return new CallExpression(this.function, partialEvaluatedParams);

	}
}
