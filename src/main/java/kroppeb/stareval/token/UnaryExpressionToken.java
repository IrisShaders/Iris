package kroppeb.stareval.token;

import kroppeb.stareval.parser.UnaryOp;

public class UnaryExpressionToken extends ExpressionToken {
	private final UnaryOp op;
	private ExpressionToken inner;

	public UnaryExpressionToken(UnaryOp op, ExpressionToken inner) {
		this.op = op;
		this.inner = inner;
	}

	@Override
	public String toString() {
		return "UnaryExpr{" + this.op + " {" + this.inner + "} }";
	}

	@Override
	public ExpressionToken simplify() {
		this.inner = this.inner.simplify();
		return this;
	}
}
