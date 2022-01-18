package net.coderbot.iris.mixin.texture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.coderbot.iris.texture.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture {
	@Shadow
	protected int id;

	@Inject(method = "getId()I", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", shift = Shift.BY, by = 2))
	private void onGenerateId(CallbackInfoReturnable<Integer> cir) {
		TextureTracker.INSTANCE.trackTexture(id, (AbstractTexture) (Object) this);
	}
}
