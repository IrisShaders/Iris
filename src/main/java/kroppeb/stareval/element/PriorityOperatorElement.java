package kroppeb.stareval.element;

public interface PriorityOperatorElement extends Element {
	int getPriority();

	Expression resolveWith(Expression right);
}
