package kroppeb.stareval.token;

import java.util.List;

public class ArgsToken extends ExpressionToken {
	public final List<ExpressionToken> tokens;

	public ArgsToken(List<ExpressionToken> tokens) {
		this.tokens = tokens;
	}

	@Override
	public String toString() {
		return "Args{" + this.tokens + "}";
	}

	@Override
	public ExpressionToken simplify() {
		if (this.tokens.size() != 1) {
			throw new RuntimeException("Brackets that aren't a call, but have 0 or more than 2 items");
		}
		return this.tokens.get(0).simplify();
	}
}
