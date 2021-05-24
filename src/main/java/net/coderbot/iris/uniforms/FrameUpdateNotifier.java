package net.coderbot.iris.uniforms;

import java.util.ArrayList;
import java.util.List;

public class FrameUpdateNotifier {
	// TODO: Make this specific to the current Pipeline object.
	public static final FrameUpdateNotifier INSTANCE = new FrameUpdateNotifier();

	private final List<Runnable> listeners;

	private FrameUpdateNotifier() {
		listeners = new ArrayList<>();
	}

	public void addListener(Runnable onNewFrame) {
		listeners.add(onNewFrame);
	}

	public void onNewFrame() {
		listeners.forEach(Runnable::run);
	}
}
