package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

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

	@Nullable
	public PBRAtlasTexture getNormalAtlas() {
		return normalAtlas;
	}

	@Nullable
	public PBRAtlasTexture getSpecularAtlas() {
		return specularAtlas;
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

	public void clear() {
		if (normalAtlas != null) {
			normalAtlas.clear();
		}
		if (specularAtlas != null) {
			specularAtlas.clear();
		}
	}

	public void reload(TextureAtlas.Preparations preparations) {
		if (normalAtlas != null) {
			normalAtlas.reload(preparations);
		}
		if (specularAtlas != null) {
			specularAtlas.reload(preparations);
		}
	}

	public void cycleAnimationFrames() {
		if (normalAtlas != null) {
			normalAtlas.cycleAnimationFrames();
		}
		if (specularAtlas != null) {
			specularAtlas.cycleAnimationFrames();
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
