package net.coderbot.iris.samplers;

import net.minecraft.client.renderer.texture.SimpleTexture;

import java.util.WeakHashMap;

public class SimpleTextureTracker {
	public static final SimpleTextureTracker INSTANCE = new SimpleTextureTracker();

	private final WeakHashMap<Integer, SimpleTexture> textures;

	private SimpleTextureTracker() {
		textures = new WeakHashMap<>();
	}

	public void trackTexture(int id, SimpleTexture texture) {
		textures.put(id, texture);
	}

	public SimpleTexture getTexture(int id) {
		return textures.get(id);
	}

	public void trackDeleteTextures(int id) {
		textures.remove(id);
	}
}
