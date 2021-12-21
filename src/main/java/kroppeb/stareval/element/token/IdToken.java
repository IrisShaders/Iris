package kroppeb.stareval.element.token;

import kroppeb.stareval.element.AccessibleExpressionElement;

public class IdToken extends Token implements AccessibleExpressionElement {
	public final String id;

	public IdToken(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Id{" + this.id + "}";
	}
}
