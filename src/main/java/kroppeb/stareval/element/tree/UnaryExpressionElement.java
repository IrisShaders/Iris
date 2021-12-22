package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.parser.UnaryOp;

public class UnaryExpressionElement implements ExpressionElement {
	private final UnaryOp op;
	private final ExpressionElement inner;

	public UnaryExpressionElement(UnaryOp op, ExpressionElement inner) {
		this.op = op;
		this.inner = inner;
	}

	public UnaryOp getOp() {
		return op;
	}

	public ExpressionElement getInner() {
		return inner;
	}

	@Override
	public String toString() {
		return "UnaryExpr{" + this.op + " {" + this.inner + "} }";
	}
}
