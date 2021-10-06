package net.irisshaders.iris.uniforms;

import java.util.ArrayList;
import java.util.List;

public class FrameUpdateNotifier {
	private final List<Runnable> listeners;

	public FrameUpdateNotifier() {
		listeners = new ArrayList<>();
	}

	public void addListener(Runnable onNewFrame) {
		listeners.add(onNewFrame);
	}

	public void onNewFrame() {
		listeners.forEach(Runnable::run);
	}
}
