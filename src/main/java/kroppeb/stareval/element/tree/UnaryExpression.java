package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.Expression;
import kroppeb.stareval.parser.UnaryOp;

public class UnaryExpression implements Expression {
	private final UnaryOp op;
	private Expression inner;

	public UnaryExpression(UnaryOp op, Expression inner) {
		this.op = op;
		this.inner = inner;
	}

	@Override
	public String toString() {
		return "UnaryExpr{" + this.op + " {" + this.inner + "} }";
	}

	@Override
	public Expression simplify() {
		this.inner = this.inner.simplify();
		return this;
	}
}
