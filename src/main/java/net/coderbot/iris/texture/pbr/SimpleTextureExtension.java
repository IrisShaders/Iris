package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.Nullable;

public interface SimpleTextureExtension extends PBRTextureHolder.Provider {
	@Nullable
	PBRSimpleTextureHolder getPBRHolder();
}
