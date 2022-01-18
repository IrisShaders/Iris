package net.coderbot.iris.texture;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class TextureTracker {
	public static final TextureTracker INSTANCE = new TextureTracker();

	private static Runnable bindTextureListener;

	static {
		StateUpdateNotifiers.bindTextureNotifier = listener -> bindTextureListener = listener;
	}

	// Using the nullary ctor or 0 causes errors
	private final ObjectArrayList<AbstractTexture> textures = new ObjectArrayList<>(1);

	private TextureTracker() {
	}

	public void trackTexture(int id, AbstractTexture texture) {
		if (id >= textures.size()) {
			textures.size(id + 1);
		}
		AbstractTexture oldTexture = textures.set(id, texture);
		// TODO: find more reliable way to detect when info should be reset
		if (oldTexture != texture && oldTexture instanceof TextureAtlas && texture instanceof TextureAtlas) {
			AtlasInfoGatherer.resetInfo((TextureAtlas) texture);
		}
	}

	@Nullable
	public AbstractTexture getTexture(int id) {
		if (id < textures.size()) {
			return textures.get(id);
		}
		return null;
	}

	public void onBindTexture(int id) {
		if (GlStateManagerAccessor.getActiveTexture() == 0) {
			if (bindTextureListener != null) {
				bindTextureListener.run();
			}
			Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> {
				AbstractTexture texture = getTexture(id);
				if (texture != null) {
					pipeline.setBoundTexture(texture);
				}
			});
		}
	}

	public void onDeleteTexture(int id) {
		if (id < textures.size()) {
			textures.set(id, null);
		}
	}
}
