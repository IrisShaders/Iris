package kroppeb.stareval.token;

import kroppeb.stareval.parser.BinaryOp;

public class PartialBinaryExpressionToken extends PriorityOperatorToken {
	private final ExpressionToken left;
	private final BinaryOp op;

	public PartialBinaryExpressionToken(ExpressionToken left, BinaryOp op) {
		this.left = left;
		this.op = op;
	}

	@Override
	public String toString() {
		return "PartialBinaryExpression{ {" + this.left + "} " + this.op + "}";
	}

	@Override
	public int getPriority() {
		return this.op.getPriority();
	}

	@Override
	public BinaryExpressionToken resolveWith(ExpressionToken right) {
		return new BinaryExpressionToken(this.op, this.left, right);
	}
}
