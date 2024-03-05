package net.irisshaders.iris.mixin.texture.pbr;

import net.irisshaders.iris.texture.pbr.PBRAtlasHolder;
import net.irisshaders.iris.texture.pbr.TextureAtlasExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasExtension {
	@Shadow
	@Final
	private ResourceLocation location;
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
