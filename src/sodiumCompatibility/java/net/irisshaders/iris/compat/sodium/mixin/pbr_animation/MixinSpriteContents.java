package net.irisshaders.iris.compat.sodium.mixin.pbr_animation;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.irisshaders.iris.texture.pbr.PBRSpriteHolder;
import net.irisshaders.iris.texture.pbr.SpriteContentsExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContents {
	@SuppressWarnings("all")
	@Inject(method = "sodium$setActive(Z)V", at = @At("TAIL"), remap = false)
	private void iris$onTailMarkActive(CallbackInfo ci) {
		PBRSpriteHolder pbrHolder = ((SpriteContentsExtension) this).getPBRHolder();
		if (pbrHolder != null) {
			TextureAtlasSprite normalSprite = pbrHolder.getNormalSprite();
			TextureAtlasSprite specularSprite = pbrHolder.getSpecularSprite();
			if (normalSprite != null) {
				SpriteUtil.markSpriteActive(normalSprite);
			}
			if (specularSprite != null) {
				SpriteUtil.markSpriteActive(specularSprite);
			}
		}
	}
}
