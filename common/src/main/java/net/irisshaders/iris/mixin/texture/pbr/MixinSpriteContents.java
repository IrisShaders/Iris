package net.irisshaders.iris.mixin.texture.pbr;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.irisshaders.iris.pbr.texture.PBRSpriteHolder;
import net.irisshaders.iris.pbr.texture.SpriteContentsExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public class MixinSpriteContents implements SpriteContentsExtension {
	@Unique
	private PBRSpriteHolder pbrHolder;

	@Inject(method = "close()V", at = @At("TAIL"), remap = false)
	private void iris$onTailClose(CallbackInfo ci) {
		if (pbrHolder != null) {
			pbrHolder.close();
		}
	}

	@Dynamic("Added by Sodium")
	@SuppressWarnings("target")
	@Inject(method = "sodium$setActive(Z)V", at = @At("TAIL"), remap = false, require = 0)
	private void iris$onTailMarkActive(CallbackInfo ci) {
		PBRSpriteHolder pbrHolder = ((SpriteContentsExtension) this).getPBRHolder();
		if (pbrHolder != null) {
			TextureAtlasSprite normalSprite = pbrHolder.getNormalSprite();
			TextureAtlasSprite specularSprite = pbrHolder.getSpecularSprite();
			if (normalSprite != null) {
				SpriteUtil.INSTANCE.markSpriteActive(normalSprite);
			}
			if (specularSprite != null) {
				SpriteUtil.INSTANCE.markSpriteActive(specularSprite);
			}
		}
	}

	@Override
	public PBRSpriteHolder getPBRHolder() {
		return pbrHolder;
	}

	@Override
	public PBRSpriteHolder getOrCreatePBRHolder() {
		if (pbrHolder == null) {
			pbrHolder = new PBRSpriteHolder();
		}
		return pbrHolder;
	}
}
