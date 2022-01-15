package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

import net.coderbot.iris.texture.atlas.PBRSpriteHolder;

public interface TextureAtlasSpriteExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRSpriteHolder getPBRSpriteHolder();

	PBRSpriteHolder getOrCreatePBRSpriteHolder();
}
