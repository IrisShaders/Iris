package kroppeb.stareval.token;

public abstract class ExpressionToken extends Token {
	public ExpressionToken simplify() {
		return this;
	}
}
