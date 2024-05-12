package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.ExpressionElement;

import java.util.List;

public class FunctionCall implements ExpressionElement {
	private final String id;
	private final List<? extends ExpressionElement> args;

	public FunctionCall(String id, List<? extends ExpressionElement> args) {
		this.id = id;
		this.args = args;
	}

	public String getId() {
		return this.id;
	}

	public List<? extends ExpressionElement> getArgs() {
		return this.args;
	}

	@Override
	public String toString() {
		return "FunctionCall{" + this.id + " {" + this.args + "} }";
	}
}
