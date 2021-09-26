package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.Expression;
import kroppeb.stareval.parser.BinaryOp;

public class BinaryExpression implements Expression {
	private final BinaryOp op;
	private Expression left;
	private Expression right;

	public BinaryExpression(BinaryOp op, Expression left, Expression right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "BinaryExpr{ {" + this.left + "} " + this.op + " {" + this.right + "} }";
	}

	@Override
	public Expression simplify() {
		this.left = this.left.simplify();
		this.right = this.right.simplify();
		return this;
	}
}
