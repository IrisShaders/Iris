package kroppeb.stareval.token;

import kroppeb.stareval.parser.UnaryOp;

public class UnaryOperatorToken extends PriorityOperatorToken {
	private final UnaryOp op;

	public UnaryOperatorToken(UnaryOp op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return "UnaryOp{" + this.op + "}";
	}

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public UnaryExpressionToken resolveWith(ExpressionToken right) {
		return new UnaryExpressionToken(this.op, right);
	}
}
