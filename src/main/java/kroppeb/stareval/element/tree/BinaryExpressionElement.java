package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.parser.BinaryOp;

public class BinaryExpressionElement implements ExpressionElement {
	private final BinaryOp op;
	private final ExpressionElement left;
	private final ExpressionElement right;

	public BinaryExpressionElement(BinaryOp op, ExpressionElement left, ExpressionElement right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return "BinaryExpr{ {" + this.left + "} " + this.op + " {" + this.right + "} }";
	}
}
