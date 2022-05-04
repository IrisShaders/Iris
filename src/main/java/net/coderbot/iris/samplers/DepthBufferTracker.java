package net.coderbot.iris.samplers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.coderbot.iris.gl.texture.DepthBufferFormat;

public class DepthBufferTracker {
	public static final DepthBufferTracker INSTANCE = new DepthBufferTracker();

	private final Int2ObjectMap<DepthBufferFormat> formats;

	private DepthBufferTracker() {
		formats = new Int2ObjectOpenHashMap<>();
	}

	public void trackDepthBuffer(int id) {
		formats.putIfAbsent(id, null);
	}

	public void trackTexImage2D(int id, int glformat) {
		if (formats.containsKey(id)) {
			formats.put(id, DepthBufferFormat.fromGlEnum(glformat));
		}
	}

	public DepthBufferFormat getFormat(int id) {
		DepthBufferFormat format = formats.get(id);

		if (format == null) {
			// yolo, just assume it's GL_DEPTH_COMPONENT
			return DepthBufferFormat.DEPTH;
		}

		return format;
	}

	public void trackDeleteTextures(int id) {
		formats.remove(id);
	}
}
