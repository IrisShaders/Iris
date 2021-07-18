package net.coderbot.iris.mixin;

import net.coderbot.iris.texunits.SpriteAtlasTextureInterface;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteAtlasTexture.class)
public class MixinSpriteAtlasTexture implements SpriteAtlasTextureInterface {
	private Vec2f atlasSize;

	@Inject(method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/TextureStitcher;I)Ljava/util/List;", at = @At("HEAD"))
	private void getAtlasSize(ResourceManager resourceManager, TextureStitcher textureStitcher, int maxLevel, CallbackInfoReturnable<List<Sprite>> cir) {
		atlasSize = new Vec2f(textureStitcher.getWidth(), textureStitcher.getHeight());
	}

	@Override
	public Vec2f getAtlasSize() {
		return atlasSize;
	}
}

