package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlas;

public class PBRAtlasTextureHolder implements PBRTextureHolder {
	protected final TextureAtlas atlas;
	protected PBRAtlasTexture normalAtlas;
	protected PBRAtlasTexture specularAtlas;

	public PBRAtlasTextureHolder(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	@Override
	public boolean hasNormalTexture() {
		return normalAtlas != null;
	}

	@Override
	public boolean hasSpecularTexture() {
		return specularAtlas != null;
	}

	@Override
	@Nullable
	public PBRAtlasTexture getNormalTexture() {
		return normalAtlas;
	}

	@Override
	@Nullable
	public PBRAtlasTexture getSpecularTexture() {
		return specularAtlas;
	}

	public PBRAtlasTexture getOrCreateNormalTexture() {
		if (normalAtlas == null) {
			normalAtlas = new PBRAtlasTexture(atlas, PBRType.NORMAL);
		}
		return normalAtlas;
	}

	public PBRAtlasTexture getOrCreateSpecularTexture() {
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
