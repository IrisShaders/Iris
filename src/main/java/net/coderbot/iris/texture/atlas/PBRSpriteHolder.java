package net.coderbot.iris.texture.atlas;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class PBRSpriteHolder {
	protected TextureAtlasSprite normalSprite;
	protected TextureAtlasSprite specularSprite;

	public TextureAtlasSprite getNormalSprite() {
		return normalSprite;
	}

	public TextureAtlasSprite getSpecularSprite() {
		return specularSprite;
	}

	public void setNormalSprite(TextureAtlasSprite sprite) {
		normalSprite = sprite;
	}

	public void setSpecularSprite(TextureAtlasSprite sprite) {
		specularSprite = sprite;
	}

	// TODO: won't work with Sodium
	public void tickAnimation() {
		if (normalSprite != null) {
			normalSprite.cycleFrames();
		}
		if (specularSprite != null) {
			specularSprite.cycleFrames();
		}
	}

	public void close() {
		if (normalSprite != null) {
			normalSprite.close();
		}
		if (specularSprite != null) {
			specularSprite.close();
		}
	}
}
