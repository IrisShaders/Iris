package kroppeb.stareval.token;

import kroppeb.stareval.parser.UnaryOp;

public class UnaryExpressionToken extends ExpressionToken {
	public final UnaryOp op;
	public ExpressionToken inner;
	
	public UnaryExpressionToken(UnaryOp op, ExpressionToken inner) {
		this.op = op;
		this.inner = inner;
	}
	
	@Override
	public String toString() {
		return "UnaryExpr{" + op + " {" + inner + "} }";
	}
	
	@Override
	public ExpressionToken simplify() {
		this.inner = inner.simplify();
		return this;
	}
}
