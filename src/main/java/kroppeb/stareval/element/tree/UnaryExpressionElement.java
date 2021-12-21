package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.parser.UnaryOp;

public class UnaryExpressionElement implements ExpressionElement {
	private final UnaryOp op;
	private ExpressionElement inner;

	public UnaryExpressionElement(UnaryOp op, ExpressionElement inner) {
		this.op = op;
		this.inner = inner;
	}

	@Override
	public String toString() {
		return "UnaryExpr{" + this.op + " {" + this.inner + "} }";
	}

	@Override
	public ExpressionElement simplify() {
		this.inner = this.inner.simplify();
		return this;
	}
}
