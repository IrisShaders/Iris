package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasSpriteExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRAtlasSpriteHolder getPBRSpriteHolder();

	PBRAtlasSpriteHolder getOrCreatePBRSpriteHolder();
}
