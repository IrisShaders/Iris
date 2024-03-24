package net.irisshaders.iris.parsing;

import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.function.TypedFunction;

import java.util.Collection;

public class BooleanVectorizedFunction implements TypedFunction {
	final TypedFunction inner;
	final int size;
	final Parameter[] parameters;

	private final ElementAccessExpression[] vectorAccessors;
	private int index;

	public BooleanVectorizedFunction(TypedFunction inner, int size) {
		this.inner = inner;
		this.size = size;

		Parameter[] innerTypes = inner.getParameters();
		this.parameters = new Parameter[innerTypes.length];
		this.vectorAccessors = new ElementAccessExpression[innerTypes.length];

		for (int i = 0; i < innerTypes.length; i++) {
			this.parameters[i] = new Parameter(VectorType.of((Type.Primitive) innerTypes[i].type(), size));
			this.vectorAccessors[i] = new ElementAccessExpression(innerTypes[i].type());
		}
	}

	@Override
	public Type getReturnType() {
		return Type.Boolean;
	}

	@Override
	public Parameter[] getParameters() {
		return this.parameters;
	}

	@Override
	public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
		for (int p = 0; p < params.length; p++) {
			final Expression param = params[p];
			param.evaluateTo(context, functionReturn);
			this.vectorAccessors[p].vector = functionReturn.objectReturn;
		}

		for (int i = 0; i < this.size; i++) {
			this.index = i;
			this.inner.evaluateTo(this.vectorAccessors, context, functionReturn);
			if (!functionReturn.booleanReturn)
				return; // if false -> return false
		}

		// if all true (and thus the last one was true) -> return true
	}

	class ElementAccessExpression implements Expression {
		final Type parameterType;
		Object vector;


		ElementAccessExpression(Type parameterType) {
			this.parameterType = parameterType;
		}

		@Override
		public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
			parameterType.getValueFromArray(vector, index, functionReturn);
		}

		@Override
		public void listVariables(Collection<? super VariableExpression> variables) {
			throw new IllegalStateException();
		}
	}
}
