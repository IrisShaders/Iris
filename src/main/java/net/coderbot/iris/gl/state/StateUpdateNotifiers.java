package net.coderbot.iris.gl.state;

import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;

/**
 * Holds some standard update notifiers for various elements of GL state. Currently, this class has a few listeners for
 * fog-related values.
 */
public class StateUpdateNotifiers {
	public static ValueUpdateNotifier fogStartNotifier;
	public static ValueUpdateNotifier fogEndNotifier;
	public static ValueUpdateNotifier blendFuncNotifier;
	public static ValueUpdateNotifier atlasTextureNotifier;
	public static ValueUpdateNotifier phaseChangeNotifier;
}
