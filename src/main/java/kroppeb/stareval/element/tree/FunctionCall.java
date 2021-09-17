package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.Expression;

import java.util.List;

public class FunctionCall implements Expression {
	private final String id;
	private final List<Expression> args;

	public FunctionCall(String id, List<Expression> args) {
		this.id = id;
		this.args = args;
	}

	@Override
	public String toString() {
		return "FunctionCall{" + this.id + " {" + this.args + "} }";
	}

	@Override
	public Expression simplify() {
		for (int i = 0; i < this.args.size(); i++) {
			this.args.set(i, this.args.get(i).simplify());
		}

		return this;
	}
}
