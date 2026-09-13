package net.irisshaders.iris.gl.blending;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlendModeStorage {
	private static final Map<Integer, BlendMode> buffers = new HashMap<>();
	private static boolean overridden;
	private static BlendMode blend;

	private BlendModeStorage() {
	}

	public static void overrideBlend(BlendMode override) {
		overridden = true;
		blend = override;
		buffers.clear();
	}

	public static void overrideBufferBlend(int index, BlendMode override) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative blend attachment");
		}

		buffers.put(index, override);
	}

	public static List<BlendMode> snapshot(int attachments, BlendMode defaultBlend) {
		List<BlendMode> states = new ArrayList<>(attachments);

		for (int i = 0; i < attachments; i++) {
			states.add(buffers.containsKey(i) ? buffers.get(i) : overridden ? blend : defaultBlend);
		}

		return Collections.unmodifiableList(states);
	}

	public static void restoreBlend() {
		overridden = false;
		blend = null;
		buffers.clear();
	}
}
