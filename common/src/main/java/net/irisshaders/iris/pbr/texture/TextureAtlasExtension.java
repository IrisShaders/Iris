package net.irisshaders.iris.pbr.texture;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension {
	@Nullable
	PBRAtlasHolder getPBRHolder();

	PBRAtlasHolder getOrCreatePBRHolder();
}
