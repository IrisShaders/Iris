package kroppeb.stareval.element.tree.partial;

import kroppeb.stareval.element.PriorityOperatorElement;
import kroppeb.stareval.element.tree.BinaryExpression;
import kroppeb.stareval.element.Expression;
import kroppeb.stareval.parser.BinaryOp;

public class PartialBinaryExpression extends PartialExpression implements PriorityOperatorElement {
	private final Expression left;
	private final BinaryOp op;

	public PartialBinaryExpression(Expression left, BinaryOp op) {
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
	public BinaryExpression resolveWith(Expression right) {
		return new BinaryExpression(this.op, this.left, right);
	}
}
