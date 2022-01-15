package net.coderbot.iris.mixin.pbr;

import net.coderbot.iris.texture.atlas.TextureAtlasSpriteExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteAnimatedTextureMixin {
	@Inject(method = "cycleFrames", at = @At("TAIL"))
	private void onTailTick(CallbackInfo ci) {
		TextureAtlasSpriteExtension extension = (TextureAtlasSpriteExtension) this;
		if (extension.hasPBRSpriteHolder()) {
			extension.getPBRSpriteHolder().tickAnimation();
		}
	}
}
