package net.coderbot.iris.parsing;

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
	final VectorType[] parameterTypes;
	
	private final ElementAccessExpression[] vectorAccessors;
	private int index;
	
	public BooleanVectorizedFunction(TypedFunction inner, int size) {
		this.inner = inner;
		this.size = size;
		
		Type[] innerTypes = inner.getParameterTypes();
		this.parameterTypes = new VectorType[innerTypes.length];
		this.vectorAccessors = new ElementAccessExpression[innerTypes.length];
		
		for (int i = 0; i < innerTypes.length; i++) {
			this.parameterTypes[i] = VectorType.of((Type.Primitive) innerTypes[i], size);
			this.vectorAccessors[i] = new ElementAccessExpression(innerTypes[i]);
		}
	}
	
	@Override
	public Type getReturnType() {
		return Type.Boolean;
	}
	
	@Override
	public Type[] getParameterTypes() {
		return this.parameterTypes;
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
			if(!functionReturn.booleanReturn)
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
