package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.AbstractTexture;

public interface PBRTextureHolder {
	boolean hasNormalTexture();

	boolean hasSpecularTexture();

	@Nullable
	AbstractTexture getNormalTexture();

	@Nullable
	AbstractTexture getSpecularTexture();

	public interface Provider {
		boolean hasPBRHolder();

		@Nullable
		PBRTextureHolder getPBRHolder();
	}
}
