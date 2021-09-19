package kroppeb.stareval.element.token;

import kroppeb.stareval.element.PriorityOperatorElement;
import kroppeb.stareval.element.Expression;
import kroppeb.stareval.element.tree.UnaryExpression;
import kroppeb.stareval.parser.UnaryOp;

public class UnaryOperatorToken extends Token implements PriorityOperatorElement {
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
	public UnaryExpression resolveWith(Expression right) {
		return new UnaryExpression(this.op, right);
	}
}
