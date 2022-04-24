package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRAtlasHolder;
import net.coderbot.iris.texture.pbr.TextureAtlasExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasExtension {
	@Unique
	private PBRAtlasHolder pbrHolder;

	@Inject(method = "cycleAnimationFrames()V", at = @At("TAIL"))
	private void iris$onTailCycleAnimationFrames(CallbackInfo ci) {
		if (pbrHolder != null) {
			pbrHolder.cycleAnimationFrames();
		}
	}

	@Override
	public PBRAtlasHolder getPBRHolder() {
		return pbrHolder;
	}

	@Override
	public PBRAtlasHolder getOrCreatePBRHolder() {
		if (pbrHolder == null) {
			pbrHolder = new PBRAtlasHolder();
		}
		return pbrHolder;
	}
}
