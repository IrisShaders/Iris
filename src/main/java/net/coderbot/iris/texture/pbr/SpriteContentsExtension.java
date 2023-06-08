package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface SpriteContentsExtension {
	@Nullable
	PBRSpriteHolder getPBRHolder();

	PBRSpriteHolder getOrCreatePBRHolder();
}
