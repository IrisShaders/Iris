package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRAtlasTexture;
import net.coderbot.iris.texture.pbr.PBRTextureHolder;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture {
	@Inject(method = "cycleAnimationFrames()V", at = @At("TAIL"))
	private void onTailCycleAnimationFrames(CallbackInfo ci) {
		PBRTextureHolder holder = PBRTextureManager.INSTANCE.getHolder(getId());
		AbstractTexture normalTexture = holder.normalTexture();
		AbstractTexture specularTexture = holder.specularTexture();
		if (normalTexture instanceof PBRAtlasTexture) {
			PBRAtlasTexture normalAtlas = (PBRAtlasTexture) normalTexture;
			normalAtlas.cycleAnimationFrames();
		}
		if (specularTexture instanceof PBRAtlasTexture) {
			PBRAtlasTexture specularAtlas = (PBRAtlasTexture) specularTexture;
			specularAtlas.cycleAnimationFrames();
		}
	}
}
