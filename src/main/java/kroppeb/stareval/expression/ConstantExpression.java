package kroppeb.stareval.expression;

import kroppeb.stareval.function.Type;

public abstract class ConstantExpression implements Expression{
	private final Type type;
	
	protected ConstantExpression(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
}
