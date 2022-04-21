package net.coderbot.iris.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.opengl.GL20C;

public class AtlasInfoGatherer {
	public static int getWidth(TextureAtlas atlas) {
		int width = ((TextureAtlasInterface) atlas).getWidth();
		if (width == -1) {
			fetchAtlasSizeFromGlState(atlas);
			width = ((TextureAtlasInterface) atlas).getWidth();
		}
		return width;
	}

	public static int getHeight(TextureAtlas atlas) {
		int height = ((TextureAtlasInterface) atlas).getHeight();
		if (height == -1) {
			fetchAtlasSizeFromGlState(atlas);
			height = ((TextureAtlasInterface) atlas).getHeight();
		}
		return height;
	}

	public static int getMipLevel(TextureAtlas atlas) {
		int mipLevel = ((TextureAtlasInterface) atlas).getMipLevel();
		if (mipLevel == -1) {
			fetchAtlasMipLevel(atlas);
			mipLevel = ((TextureAtlasInterface) atlas).getMipLevel();
		}
		return mipLevel;
	}

	public static void resetInfo(TextureAtlas atlas) {
		TextureAtlasInterface atlasI = (TextureAtlasInterface) atlas;
		atlasI.setWidth(-1);
		atlasI.setHeight(-1);
		atlasI.setMipLevel(-1);
	}

	/**
	 * Reliable and supports DashLoader (and other mods which might mess with the other code path)
	 *
	 * @author Kroppeb
	 */
	private static void fetchAtlasSizeFromGlState(TextureAtlas atlas) {
		// Keep track of what texture was bound before
		int existingGlId = GlStateManager._getInteger(GL20C.GL_TEXTURE_BINDING_2D);

		// Bind this texture and grab the atlas size from it.
		RenderSystem.bindTexture(atlas.getId());
		int width = GlStateManager._getTexLevelParameter(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_WIDTH);
		int height = GlStateManager._getTexLevelParameter(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_HEIGHT);

		TextureAtlasInterface atlasI = (TextureAtlasInterface) atlas;
		atlasI.setWidth(width);
		atlasI.setHeight(height);

		// Make sure to re-bind the previous texture to avoid issues.
		RenderSystem.bindTexture(existingGlId);
	}

	private static void fetchAtlasMipLevel(TextureAtlas atlas) {
		TextureAtlasSprite missingSprite = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
		int mipLevel = ((TextureAtlasSpriteAccessor) missingSprite).getMainImage().length - 1;

		TextureAtlasInterface atlasI = (TextureAtlasInterface) atlas;
		atlasI.setMipLevel(mipLevel);
	}
}
