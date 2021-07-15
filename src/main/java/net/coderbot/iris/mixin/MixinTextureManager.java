package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Redirect(method = "bindTextureInner", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/AbstractTexture;bindTexture()V"))
	private void setAtlas(AbstractTexture abstractTexture) {
		if(abstractTexture instanceof SpriteAtlasTexture) {
			CapturedRenderingState.INSTANCE.setCurrentAtlas((SpriteAtlasTexture) abstractTexture);
		}
		abstractTexture.bindTexture();
	}
}
