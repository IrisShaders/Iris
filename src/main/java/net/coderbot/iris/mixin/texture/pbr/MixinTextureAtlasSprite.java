package net.coderbot.iris.mixin.texture.pbr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.texture.pbr.PBRAtlasSpriteHolder;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
public class MixinTextureAtlasSprite implements TextureAtlasSpriteExtension {
	@Unique
	private PBRAtlasSpriteHolder pbrHolder;

//	@Inject(method = "cycleFrames", at = @At("TAIL"))
//	private void onTailTick(CallbackInfo ci) {
//		TextureAtlasSpriteExtension extension = (TextureAtlasSpriteExtension) this;
//		if (extension.hasPBRSpriteHolder()) {
//			extension.getPBRSpriteHolder().tickAnimation();
//		}
//	}

	@Inject(method = "close()V", at = @At("TAIL"), remap = false)
	private void onTailClose(CallbackInfo ci) {
		if (pbrHolder != null) {
			pbrHolder.close();
		}
	}

	@Override
	public boolean hasPBRSpriteHolder() {
		return pbrHolder != null;
	}

	@Override
	public PBRAtlasSpriteHolder getPBRSpriteHolder() {
		return pbrHolder;
	}

	@Override
	public PBRAtlasSpriteHolder getOrCreatePBRSpriteHolder() {
		if (pbrHolder == null) {
			pbrHolder = new PBRAtlasSpriteHolder();
		}
		return pbrHolder;
	}
}
