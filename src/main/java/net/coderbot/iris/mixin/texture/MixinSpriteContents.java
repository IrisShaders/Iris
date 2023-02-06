package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpriteContents.class)
public class MixinSpriteContents {
	@Redirect(method = "increaseMipLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels([Lcom/mojang/blaze3d/platform/NativeImage;I)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private NativeImage[] iris$redirectMipmapGeneration(NativeImage[] nativeImages, int mipLevel) {
		if (this instanceof CustomMipmapGenerator.Provider) {
			CustomMipmapGenerator.Provider provider = (CustomMipmapGenerator.Provider) this;
			CustomMipmapGenerator generator = provider.getMipmapGenerator();
			if (generator != null) {
				try {
					return generator.generateMipLevels(nativeImages, mipLevel);
				} catch (Exception e) {
					Iris.logger.error("ERROR MIPMAPPING", e);
				}
			}
		}
		return MipmapGenerator.generateMipLevels(nativeImages, mipLevel);
	}
}
