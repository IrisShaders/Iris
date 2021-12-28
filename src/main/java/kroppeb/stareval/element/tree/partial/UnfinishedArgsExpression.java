package kroppeb.stareval.element.tree.partial;

import kroppeb.stareval.element.ExpressionElement;

import java.util.ArrayList;
import java.util.List;

public class UnfinishedArgsExpression extends PartialExpression {
	public final List<ExpressionElement> tokens = new ArrayList<>();

	@Override
	public String toString() {
		return "UnfinishedArgs{" + this.tokens + "}";
	}
}
