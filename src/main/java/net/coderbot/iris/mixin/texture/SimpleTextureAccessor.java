package net.coderbot.iris.mixin.texture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;

@Mixin(SimpleTexture.class)
public interface SimpleTextureAccessor {
	@Accessor("location")
	ResourceLocation getLocation();
}
