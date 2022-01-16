package net.coderbot.iris.texture.atlas;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRAtlasHolder getPBRAtlasHolder();
}
