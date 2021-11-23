package net.coderbot.iris.gl.state;

import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;

/**
 * Holds some standard update notifiers for various elements of GL state. Currently, this class has a few listeners for
 * fog-related values.
 */
public class StateUpdateNotifiers {
	public static ValueUpdateNotifier fogToggleNotifier;
	public static ValueUpdateNotifier fogModeNotifier;
	public static ValueUpdateNotifier fogDensityNotifier;
	public static ValueUpdateNotifier atlasTextureNotifier;
}
