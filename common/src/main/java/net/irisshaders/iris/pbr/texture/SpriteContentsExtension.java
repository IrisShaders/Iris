package net.irisshaders.iris.pbr.texture;

import org.jetbrains.annotations.Nullable;

public interface SpriteContentsExtension {
	@Nullable
	PBRSpriteHolder getPBRHolder();

	PBRSpriteHolder getOrCreatePBRHolder();
}
