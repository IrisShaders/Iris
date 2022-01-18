package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface SimpleTextureExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRSimpleTextureHolder getPBRSpriteHolder();
}
