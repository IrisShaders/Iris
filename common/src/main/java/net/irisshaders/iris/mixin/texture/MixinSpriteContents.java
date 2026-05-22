package net.irisshaders.iris.mixin.texture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
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
	@WrapOperation(method = "increaseMipLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;generateMipLevels(Lnet/minecraft/resources/Identifier;[Lcom/mojang/blaze3d/platform/NativeImage;ILnet/minecraft/client/renderer/texture/MipmapStrategy;FLcom/mojang/blaze3d/platform/Transparency;)[Lcom/mojang/blaze3d/platform/NativeImage;"))
	private NativeImage[] iris$redirectMipmapGeneration(final Identifier name, final NativeImage[] currentMips, final int newMipLevel, MipmapStrategy mipmapStrategy, final float alphaCutoffBias, final Transparency transparency, Operation<NativeImage[]> original) {
		if (this instanceof CustomMipmapGenerator.Provider provider) {
			CustomMipmapGenerator generator = provider.getMipmapGenerator();
			if (generator != null) {
				try {
					return generator.generateMipLevels(currentMips, newMipLevel);
				} catch (Exception e) {
					Iris.logger.error("ERROR MIPMAPPING", e);
				}
			}
		}
		return original.call(name, currentMips, newMipLevel, mipmapStrategy, alphaCutoffBias, transparency);
	}
}
