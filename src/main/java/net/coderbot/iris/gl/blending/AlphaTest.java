package net.coderbot.iris.gl.blending;

public class AlphaTest {
	public static final AlphaTest ALWAYS = new AlphaTest(AlphaTestFunction.ALWAYS, 0.0f);

	private final AlphaTestFunction function;
	private final float reference;

	public AlphaTest(AlphaTestFunction function, float reference) {
		this.function = function;
		this.reference = reference;
	}

	public String toExpression(String indentation) {
		if (function == AlphaTestFunction.ALWAYS) {
			return "// alpha test disabled\n";
		} else if (function == AlphaTestFunction.NEVER) {
			return "discard;\n";
		}

		String expr = function.getExpression();

		return indentation + "if (!(gl_FragData[0].a " + expr + " " + reference + ")) {\n" +
				indentation + "    discard;\n" +
				indentation + "}\n";
	}

	public AlphaTestFunction getFunction() {
		return function;
	}

	public float getReference() {
		return reference;
	}
}
