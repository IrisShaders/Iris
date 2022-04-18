package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasSpriteExtension {
	boolean hasPBRHolder();

	@Nullable
	PBRSpriteHolder getPBRHolder();

	PBRSpriteHolder getOrCreatePBRHolder();
}
