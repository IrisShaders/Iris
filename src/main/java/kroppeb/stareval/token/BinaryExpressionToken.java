package kroppeb.stareval.token;

import kroppeb.stareval.parser.BinaryOp;

public class BinaryExpressionToken extends ExpressionToken {
	private final BinaryOp op;
	private ExpressionToken left;
	private ExpressionToken right;

	public BinaryExpressionToken(BinaryOp op, ExpressionToken left, ExpressionToken right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "BinaryExpr{ {" + this.left + "} " + this.op + " {" + this.right + "} }";
	}

	@Override
	public ExpressionToken simplify() {
		this.left = this.left.simplify();
		this.right = this.right.simplify();
		return this;
	}
}
