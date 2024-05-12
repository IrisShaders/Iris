package kroppeb.stareval.element.token;

import kroppeb.stareval.element.ExpressionElement;

public class NumberToken extends Token implements ExpressionElement {
	private final String number;

	public NumberToken(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Number{" + this.number + "}";
	}
}
