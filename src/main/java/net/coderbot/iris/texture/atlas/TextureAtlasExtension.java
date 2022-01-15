package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

import net.coderbot.iris.texture.atlas.PBRAtlasHolder;

public interface TextureAtlasExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRAtlasHolder getPBRAtlasHolder();
}
