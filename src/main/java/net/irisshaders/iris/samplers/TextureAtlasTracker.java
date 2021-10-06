package net.irisshaders.iris.samplers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.texunits.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.opengl.GL20C;

import java.util.WeakHashMap;

public class TextureAtlasTracker {
	public static final TextureAtlasTracker INSTANCE = new TextureAtlasTracker();

	private final WeakHashMap<Integer, TextureAtlas> atlases;

	private TextureAtlasTracker() {
		atlases = new WeakHashMap<>();
	}

	public void trackAtlas(int id, TextureAtlas atlas) {
		TextureAtlas previous = atlases.put(id, atlas);

		if (previous != atlas) {
			TextureAtlasInterface atlasI = (TextureAtlasInterface) atlas;
			atlasI.setAtlasSize(0, 0);
		}
	}

	public void trackTexImage2D(int id, int level, int sizeX, int sizeY) {
		if (level != 0) {
			return;
		}

		TextureAtlas atlas = atlases.get(id);

		if (atlas != null) {
			((TextureAtlasInterface) atlas).setAtlasSize(sizeX, sizeY);
		}
	}

	public Vec2 getAtlasSize(int id) {
		TextureAtlas atlas = atlases.get(id);
		Vec2 size = Vec2.ZERO;

		if (atlas != null) {
			size = ((TextureAtlasInterface) atlas).getAtlasSize();

			if (Vec2.ZERO.equals(size)) {
				fetchAtlasSizeFromGlState(atlas);
				size = ((TextureAtlasInterface) atlas).getAtlasSize();
			}
		}

		return size;
	}

	public void trackDeleteTextures(int id) {
		atlases.remove(id);
	}

	/**
	 * Fallback path to support DashLoader (and other mods which might mess with the other code path)
	 *
	 * @author Kroppeb
	 */
	private void fetchAtlasSizeFromGlState(TextureAtlas atlas) {
		// Keep track of what texture was bound before
		int existingGlId = GL20C.glGetInteger(GL20C.GL_TEXTURE_BINDING_2D);

		// Bind this texture and grab the atlas size from it.
		RenderSystem.bindTexture(atlas.getId());
		int width = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_WIDTH);
		int height = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_HEIGHT);

		TextureAtlasInterface atlasI = (TextureAtlasInterface) atlas;
		atlasI.setAtlasSize(width, height);

		// Make sure to re-bind the previous texture to avoid issues.
		RenderSystem.bindTexture(existingGlId);
	}
}
