package kroppeb.stareval.token;

public class NumberToken extends ExpressionToken {
	private final String number;

	public NumberToken(String number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Number{" + this.number + "}";
	}
}
