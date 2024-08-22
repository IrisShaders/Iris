package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;

import java.util.List;

public record FunctionCall(String id, List<? extends ExpressionElement> args) implements ExpressionElement {


	@Override
	public String toString() {
		return "FunctionCall{" + this.id + " {" + this.args + "} }";
	}
}
