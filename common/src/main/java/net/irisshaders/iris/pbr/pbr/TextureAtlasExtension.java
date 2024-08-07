package net.irisshaders.iris.pbr.pbr;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension {
	@Nullable
	PBRAtlasHolder getPBRHolder();

	PBRAtlasHolder getOrCreatePBRHolder();
}
