package net.coderbot.iris.mixin;

import net.coderbot.iris.texunits.SpriteAtlasTextureInterface;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteAtlasTexture.class)
public class MixinSpriteAtlasTexture implements SpriteAtlasTextureInterface {
	private Vec2f atlasSize;

	@Inject(method = "loadSprite", at = @At("HEAD"))
	private void getAtlasSize(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		atlasSize = new Vec2f(atlasWidth, atlasHeight);
	}

	@Override
	public Vec2f getAtlasSize() {
		return atlasSize;
	}
}
