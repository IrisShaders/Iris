package net.irisshaders.iris.gl.blending;


import java.util.Optional;

public enum AlphaTestFunction {
	NEVER(null),
	LESS("<"),
	EQUAL("=="),
	LEQUAL("<="),
	GREATER(">"),
	NOTEQUAL("!="),
	GEQUAL(">="),
	ALWAYS(null);

	private final String expression;

	AlphaTestFunction(String expression) {
		this.expression = expression;
	}

	public static Optional<AlphaTestFunction> fromString(String name) {
		if ("GL_ALWAYS".equals(name)) {
			// shaders.properties states that GL_ALWAYS is the name to use, but I haven't verified that this actually
			// matches the implementation... All of the other names do not have the GL_ prefix.
			//
			// We'll support it here just to be safe, even though just a plain ALWAYS seems more likely to be what it
			// parses.
			return Optional.of(AlphaTestFunction.ALWAYS);
		}

		try {
			return Optional.of(AlphaTestFunction.valueOf(name));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}


	public String getExpression() {
		return expression;
	}
}
