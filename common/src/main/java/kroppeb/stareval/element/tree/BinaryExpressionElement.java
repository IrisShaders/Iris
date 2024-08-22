package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.parser.BinaryOp;

public record BinaryExpressionElement(BinaryOp op, ExpressionElement left,
									  ExpressionElement right) implements ExpressionElement {


	@Override
	public String toString() {
		return "BinaryExpr{ {" + this.left + "} " + this.op + " {" + this.right + "} }";
	}
}
