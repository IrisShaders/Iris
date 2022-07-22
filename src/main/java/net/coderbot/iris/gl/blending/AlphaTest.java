package net.coderbot.iris.gl.blending;

import net.coderbot.iris.pipeline.newshader.AlphaTests;

public class AlphaTest {
	public static final AlphaTest ALWAYS = new AlphaTest(AlphaTestFunction.ALWAYS, 0.0f);

	private final AlphaTestFunction function;
	private final float reference;

	public AlphaTest(AlphaTestFunction function, float reference) {
		this.function = function;
		this.reference = reference;
	}

	public String toExpression(String indentation) {
		return toExpression("gl_FragData[0].a", "iris_currentAlphaTest", indentation);
	}

	public String toExpression(String alphaAccessor, String alphaThreshold, String indentation) {
		String expr = function.getExpression();

		if (function == AlphaTestFunction.ALWAYS) {
			return "// alpha test disabled\n";
		} else if (this == AlphaTests.VERTEX_ALPHA) {
			return indentation + "if (!(" + alphaAccessor + " > iris_vertexColor.a)) {\n" +
				indentation + "    discard;\n" +
				indentation + "}\n";
		} else if (function == AlphaTestFunction.NEVER) {
			return "discard;\n";
		}

		return indentation + "if (!(" + alphaAccessor + " " + expr + " " + alphaThreshold + ")) {\n" +
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
