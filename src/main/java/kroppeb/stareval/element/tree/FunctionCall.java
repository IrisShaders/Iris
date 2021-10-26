package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;

import java.util.List;

public class FunctionCall implements ExpressionElement {
	private final String id;
	private final List<ExpressionElement> args;

	public FunctionCall(String id, List<ExpressionElement> args) {
		this.id = id;
		this.args = args;
	}

	@Override
	public String toString() {
		return "FunctionCall{" + this.id + " {" + this.args + "} }";
	}
}
