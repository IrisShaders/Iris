package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture {
	@Shadow
	protected int id;

	// Inject after the newly-generated texture ID has been stored into the id field
	@Inject(method = "getId()I", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", shift = Shift.BY, by = 2))
	private void iris$afterGenerateId(CallbackInfoReturnable<Integer> cir) {
		TextureTracker.INSTANCE.trackTexture(id, (AbstractTexture) (Object) this);
	}
}
