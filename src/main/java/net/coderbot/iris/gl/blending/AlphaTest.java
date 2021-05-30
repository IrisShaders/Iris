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

	public void setup() {
		// TODO(21w10a): Replace alpha test
		throw new IllegalStateException("Cannot setup alpha state on core profile");
		/*GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(function.getGlId(), reference);*/
	}

	public static void teardown() {
		// TODO(21w10a): Replace alpha test
		throw new IllegalStateException("Cannot teardown alpha state on core profile");
	}

	@Override
	public String toString() {
		return "AlphaTestOverride { " + function + " " + reference + " }";
	}

	public static class Off extends AlphaTest {
		public Off() {
			super(null, 0.0f);
		}

		@Override
		public void setup() {
			// no-op, alpha test doesn't exist on core profile
			// TODO(21w10a): GlStateManager.disableAlphaTest();
		}

		@Override
		public String toString() {
			return "AlphaTestOverride { off }";
		}
	}
}
