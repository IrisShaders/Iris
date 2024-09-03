package net.irisshaders.iris.gl.blending;

public record AlphaTest(AlphaTestFunction function, float reference) {
	public static final AlphaTest ALWAYS = new AlphaTest(AlphaTestFunction.ALWAYS, 0.0f);

	// WARNING: adding new fields requires updating hashCode and equals methods!

	public String toExpression(String indentation) {
		return toExpression("gl_FragData[0].a", "iris_currentAlphaTest", indentation);
	}

	public String toExpression(String alphaAccessor, String alphaThreshold, String indentation) {
		String expr = function.getExpression();

		if (function == AlphaTestFunction.ALWAYS) {
			return "// alpha test disabled\n";
		} else if (this == AlphaTests.VERTEX_ALPHA) {
			return indentation + "if (!(" + alphaAccessor + " > iris_vertexColorAlpha)) {\n" +
				indentation + "    discard;\n" +
				indentation + "}\n";
		} else if (function == AlphaTestFunction.NEVER) {
			return "discard;\n";
		}

		return indentation + "if (!(" + alphaAccessor + " " + expr + " " + alphaThreshold + ")) {\n" +
			indentation + "    discard;\n" +
			indentation + "}\n";
	}

	public String toBoolean(String alphaAccessor, String alphaThreshold) {
		if (function == AlphaTestFunction.ALWAYS) {
			return "true";
		}

		if (this == AlphaTests.VERTEX_ALPHA) {
			return alphaAccessor + " <= irisInt_vertexColor.a";
		}

		return alphaAccessor + " " + function.getExpression() + " " + alphaThreshold;
	}

	public String toBoolean(String alphaAccessor, String alphaThreshold, String vertexColorA) {
		if (function == AlphaTestFunction.ALWAYS) {
			return "true";
		}

		if (this == AlphaTests.VERTEX_ALPHA) {
			// TODO IMS
			return "false";
			//return alphaAccessor + " <= " + vertexColorA;
		}

		return alphaAccessor + " " + function.getExpression() + " " + alphaThreshold;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlphaTest other = (AlphaTest) obj;
		if (function != other.function)
			return false;
		return Float.floatToIntBits(reference) == Float.floatToIntBits(other.reference);
	}
}
