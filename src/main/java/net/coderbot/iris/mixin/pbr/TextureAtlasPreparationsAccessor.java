package net.coderbot.iris.mixin.pbr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(TextureAtlas.Preparations.class)
public interface TextureAtlasPreparationsAccessor {
	@Accessor("mipLevel")
	int getMipLevel();

	@Accessor("width")
	int getWidth();

	@Accessor("height")
	int getHeight();
}
