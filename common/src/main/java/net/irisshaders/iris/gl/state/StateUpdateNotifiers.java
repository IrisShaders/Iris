package net.irisshaders.iris.gl.state;

/**
 * Holds some standard update notifiers for various elements of GL state. Currently, this class has a few listeners for
 * fog-related values.
 */
public class StateUpdateNotifiers {
	public static ValueUpdateNotifier fogStartNotifier;
	public static ValueUpdateNotifier fogEndNotifier;
	public static ValueUpdateNotifier blendFuncNotifier;
	public static ValueUpdateNotifier bindTextureNotifier;
	public static ValueUpdateNotifier normalTextureChangeNotifier;
	public static ValueUpdateNotifier specularTextureChangeNotifier;
	public static ValueUpdateNotifier phaseChangeNotifier;
	public static ValueUpdateNotifier fallbackEntityNotifier;
}
