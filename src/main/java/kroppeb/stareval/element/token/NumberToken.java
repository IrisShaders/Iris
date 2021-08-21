package kroppeb.stareval.element.token;

import kroppeb.stareval.element.Expression;

public class NumberToken extends Token implements Expression {
	private final String number;

	public NumberToken(String number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Number{" + this.number + "}";
	}
}
