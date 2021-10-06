package net.irisshaders.iris.gl.uniform;

/**
 * A
 */
public interface ValueUpdateNotifier {
	/**
	 * Sets up a listener with this notifier. Whenever the underlying value of
	 * @param listener
	 */
	void setListener(Runnable listener);
}
