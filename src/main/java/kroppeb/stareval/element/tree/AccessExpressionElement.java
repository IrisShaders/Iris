package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.AccessibleExpression;

public class AccessExpression implements AccessibleExpression {
	private final AccessibleExpression base;
	private final String index;

	public AccessExpression(AccessibleExpression base, String index) {
		this.base = base;
		this.index = index;
	}

	@Override
	public String toString() {
		return "Access{" + this.base + "[" + this.index + "]}";
	}
}
