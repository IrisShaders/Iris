package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface TypedFunction {

	static String format(TypedFunction function, String name) {
		return String.format("%s %s(%s) (priority: %d, pure:%s)",
			function.getReturnType().toString(),
			name,
			Arrays.stream(function.getParameters())
				.map(param -> param.constant() ? "const " + param.type() : param.type().toString())
				.collect(Collectors.joining(", ")),
			function.priority(),
			function.isPure() ? "yes" : "no"
		);
	}

	Type getReturnType();

	Parameter[] getParameters();

	void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn);

	default boolean isPure() {
		return true;
	}

	default int priority() {
		return 0;
	}

	class Parameter {
		private final Type type;
		private final boolean isConstant;

		public Parameter(Type type, boolean isConstant) {
			this.type = type;
			this.isConstant = isConstant;
		}

		public Parameter(Type type) {
			this(type, false);
		}

		public Type type() {
			return this.type;
		}

		public boolean constant() {
			return this.isConstant;
		}
	}
}

