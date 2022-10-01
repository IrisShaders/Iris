package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels(Lcom/mojang/blaze3d/platform/NativeImage;I)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private NativeImage[] iris$redirectMipmapGeneration(NativeImage nativeImage, int mipLevel, TextureAtlas atlas, TextureAtlasSprite.Info info, int mipLevel1, int atlasWidth, int atlasHeight, int x, int y, NativeImage nativeImage1) {
		if (this instanceof CustomMipmapGenerator.Provider) {
			CustomMipmapGenerator.Provider provider = (CustomMipmapGenerator.Provider) this;
			CustomMipmapGenerator generator = provider.getMipmapGenerator(info, atlasWidth, atlasHeight);
			if (generator != null) {
				return generator.generateMipLevels(nativeImage, mipLevel);
			}
		}
		return MipmapGenerator.generateMipLevels(nativeImage, mipLevel);
	}
}
