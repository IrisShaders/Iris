package net.irisshaders.iris.gl.blending;

public class AlphaTests {
	public static final AlphaTest OFF = AlphaTest.ALWAYS;
	public static final AlphaTest NON_ZERO_ALPHA = new AlphaTest(AlphaTestFunction.GREATER, 0.0001F);
	public static final AlphaTest ONE_TENTH_ALPHA = new AlphaTest(AlphaTestFunction.GREATER, 0.1F);

	public static final AlphaTest VERTEX_ALPHA = new AlphaTest(AlphaTestFunction.NEVER, 0);
}
