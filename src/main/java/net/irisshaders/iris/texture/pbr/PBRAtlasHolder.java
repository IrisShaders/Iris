package net.irisshaders.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public class PBRAtlasHolder {
	protected PBRAtlasTexture normalAtlas;
	protected PBRAtlasTexture specularAtlas;

	@Nullable
	public PBRAtlasTexture getNormalAtlas() {
		return normalAtlas;
	}

	public void setNormalAtlas(PBRAtlasTexture atlas) {
		normalAtlas = atlas;
	}

	@Nullable
	public PBRAtlasTexture getSpecularAtlas() {
		return specularAtlas;
	}

	public void setSpecularAtlas(PBRAtlasTexture atlas) {
		specularAtlas = atlas;
	}

	public void cycleAnimationFrames() {
		if (normalAtlas != null) {
			normalAtlas.cycleAnimationFrames();
		}
		if (specularAtlas != null) {
			specularAtlas.cycleAnimationFrames();
		}
	}
}
