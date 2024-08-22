package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.parser.UnaryOp;

public record UnaryExpressionElement(UnaryOp op, ExpressionElement inner) implements ExpressionElement {


	@Override
	public String toString() {
		return "UnaryExpr{" + this.op + " {" + this.inner + "} }";
	}
}
