package kroppeb.stareval.token;

import java.util.List;

public class CallToken extends ExpressionToken {
	private final String id;
	private final List<ExpressionToken> args;

	public CallToken(String id, List<ExpressionToken> args) {
		this.id = id;
		this.args = args;
	}

	@Override
	public String toString() {
		return "CallToken{" + this.id + " {" + this.args + "} }";
	}

	@Override
	public ExpressionToken simplify() {
		for (int i = 0; i < this.args.size(); i++) {
			this.args.set(i, this.args.get(i).simplify());
		}

		return this;
	}
}
