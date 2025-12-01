package net.irisshaders.iris.mixin.texture.pbr;

import com.mojang.blaze3d.platform.NativeImage;
import net.irisshaders.iris.pbr.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableTexture.class)
public class MixinReloadableTexture extends AbstractTexture {
	@Inject(method = "doLoad", at = @At("RETURN"))
	private void iris$onDoLoad(NativeImage nativeImage, boolean bl, boolean bl2, CallbackInfo ci) {
		TextureTracker.INSTANCE.trackTexture(this.texture.iris$getGlId(), (AbstractTexture) this);
	}
}
