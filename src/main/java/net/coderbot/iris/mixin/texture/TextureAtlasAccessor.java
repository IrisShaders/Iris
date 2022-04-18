package net.coderbot.iris.mixin.texture;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
	@Accessor("texturesByName")
	Map<ResourceLocation, TextureAtlasSprite> getTexturesByName();

	@Invoker("getResourceLocation")
	ResourceLocation callGetResourceLocation(ResourceLocation location);
}
