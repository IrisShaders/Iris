package kroppeb.stareval.token;

public abstract class PriorityOperatorToken extends Token {
	public abstract int getPriority();
	public abstract ExpressionToken resolveWith(ExpressionToken right);
}
