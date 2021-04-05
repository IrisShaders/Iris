package kroppeb.stareval.token;

public class NumberToken extends ExpressionToken {
	public final String number;
	
	public NumberToken(String number) {
		this.number = number;
	}
	
	@Override
	public String toString() {
		return "Number{" + number + "}";
	}
}
