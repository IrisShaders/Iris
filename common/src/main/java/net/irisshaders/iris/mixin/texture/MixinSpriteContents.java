package net.irisshaders.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pbr.SpriteContentsExtension;
import net.irisshaders.iris.pbr.mipmap.CustomMipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public class MixinSpriteContents implements SpriteContentsExtension {
	@Redirect(method = "increaseMipLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels(Lnet/minecraft/resources/Identifier;[Lcom/mojang/blaze3d/platform/NativeImage;ILnet/minecraft/client/renderer/texture/MipmapStrategy;F)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private NativeImage[] iris$redirectMipmapGeneration(Identifier identifier, NativeImage[] nativeImages, int mipLevel, MipmapStrategy mipmapStrategy, float alphaCutoffBias) {
		if (this instanceof CustomMipmapGenerator.Provider provider) {
			CustomMipmapGenerator generator = provider.getMipmapGenerator();
			if (generator != null) {
				try {
					return generator.generateMipLevels(nativeImages, mipLevel);
				} catch (Exception e) {
					Iris.logger.error("ERROR MIPMAPPING", e);
				}
			}
		}
		return MipmapGenerator.generateMipLevels(identifier, nativeImages, mipLevel, mipmapStrategy, alphaCutoffBias);
	}
}
