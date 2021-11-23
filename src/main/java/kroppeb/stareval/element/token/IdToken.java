package kroppeb.stareval.element.token;

import kroppeb.stareval.element.AccessibleExpression;

public class IdToken extends Token implements AccessibleExpression {
	public final String id;

	public IdToken(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Id{" + this.id + "}";
	}
}
