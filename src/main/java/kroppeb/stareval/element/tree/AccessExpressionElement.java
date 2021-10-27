package kroppeb.stareval.element.tree;

import kroppeb.stareval.element.AccessibleExpressionElement;

public class AccessExpressionElement implements AccessibleExpressionElement {
	private final AccessibleExpressionElement base;
	private final String index;

	public AccessExpressionElement(AccessibleExpressionElement base, String index) {
		this.base = base;
		this.index = index;
	}

	public AccessibleExpressionElement getBase() {
		return this.base;
	}

	public String getIndex() {
		return this.index;
	}

	@Override
	public String toString() {
		return "Access{" + this.base + "[" + this.index + "]}";
	}
}
