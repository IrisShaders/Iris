package net.irisshaders.iris.mixin.texture;

import net.irisshaders.iris.mixinterface.AbstractTextureExtended;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public class MixinDynamicTexture {
	@Inject(method = {"upload", "setPixels"}, at = @At("HEAD"))
	private void iris$invalidateTransparency(CallbackInfo ci) {
		((AbstractTextureExtended) this).iris$setTransparency(null);
	}
}
