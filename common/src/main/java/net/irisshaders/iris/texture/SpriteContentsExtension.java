package net.irisshaders.iris.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.jetbrains.annotations.Nullable;

public interface SpriteContentsExtension {
	@Nullable
	SpriteContents.Ticker getCreatedTicker();
}
