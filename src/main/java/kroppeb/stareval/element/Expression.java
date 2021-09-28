package kroppeb.stareval.element;

public interface Expression extends Element {
	@Override
	String toString();

	default Expression simplify() {
		return this;
	}
}
