package net.irisshaders.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension {
	@Nullable
	PBRAtlasHolder getPBRHolder();

	PBRAtlasHolder getOrCreatePBRHolder();
}
