package kroppeb.stareval.token;

import kroppeb.stareval.parser.BinaryOp;

public class BinaryExpressionToken extends ExpressionToken {
	public final BinaryOp op;
	public ExpressionToken left;
	public ExpressionToken right;
	
	public BinaryExpressionToken(BinaryOp op, ExpressionToken left, ExpressionToken right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public String toString() {
		return "BinaryExpr{ {" + left + "} " + op + " {" + right + "} }";
	}
	
	@Override
	public ExpressionToken simplify() {
		this.left = left.simplify();
		this.right = right.simplify();
		return this;
	}
}
