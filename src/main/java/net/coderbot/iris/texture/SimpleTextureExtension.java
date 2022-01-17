package net.coderbot.iris.texture;

import org.jetbrains.annotations.Nullable;

public interface SimpleTextureExtension {
	boolean hasPBRSpriteHolder();

	@Nullable
	PBRSimpleTextureHolder getPBRSpriteHolder();
}
