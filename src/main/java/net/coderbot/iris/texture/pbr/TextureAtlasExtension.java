package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension extends PBRTextureHolder.Provider {
	@Nullable
	PBRAtlasTextureHolder getPBRHolder();
}
