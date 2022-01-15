package net.coderbot.iris.texture.atlas;

import net.coderbot.iris.texture.PBRType;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class PBRAtlasHolder {
	protected final TextureAtlas atlas;
	protected PBRAtlasTexture normalAtlas;
	protected PBRAtlasTexture specularAtlas;

	public PBRAtlasHolder(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	public boolean hasNormalAtlas() {
		return normalAtlas != null;
	}

	public boolean hasSpecularAtlas() {
		return specularAtlas != null;
	}

	public PBRAtlasTexture getOrCreateNormalAtlas() {
		if (normalAtlas == null) {
			normalAtlas = new PBRAtlasTexture(atlas, PBRType.NORMAL);
		}
		return normalAtlas;
	}

	public PBRAtlasTexture getOrCreateSpecularAtlas() {
		if (specularAtlas == null) {
			specularAtlas = new PBRAtlasTexture(atlas, PBRType.SPECULAR);
		}
		return specularAtlas;
	}

	public void reload(TextureAtlas.Preparations preparations) {
		if (normalAtlas != null) {
			normalAtlas.reload(preparations);
		}
		if (specularAtlas != null) {
			specularAtlas.reload(preparations);
		}
	}

	public void releaseIds() {
		if (normalAtlas != null) {
			normalAtlas.releaseId();
		}
		if (specularAtlas != null) {
			specularAtlas.releaseId();
		}
	}

	public void close() {
		if (normalAtlas != null) {
			normalAtlas.close();
		}
		if (specularAtlas != null) {
			specularAtlas.close();
		}
	}
}
