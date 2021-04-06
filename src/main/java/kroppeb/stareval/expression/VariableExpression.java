package kroppeb.stareval.expression;

import java.util.Collection;

public interface VariableExpression extends Expression {
	@Override
	default void listVariables(Collection<? super Expression> variables) {
		variables.add(this);
	}
}
