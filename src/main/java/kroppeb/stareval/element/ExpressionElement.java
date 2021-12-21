package kroppeb.stareval.element;

public interface ExpressionElement extends Element {
	@Override
	String toString();

	default ExpressionElement simplify() {
		return this;
	}
}
