package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface TextureAtlasExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRAtlasHolder getPBRAtlasHolder();
}
