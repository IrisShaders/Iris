package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasSpriteExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRSpriteHolder getPBRSpriteHolder();

	PBRSpriteHolder getOrCreatePBRSpriteHolder();
}
