package net.coderbot.iris.mixin.vertices;

import net.minecraft.client.render.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes generic vertex attributes, previously it was only possible to use a vertex attribute with a location of 0.
 */
@Mixin(VertexFormatElement.class)
public class MixinVertexFormatElement {
	@Inject(method = "isValidType(ILnet/minecraft/client/render/VertexFormatElement$Type;)Z", at = @At("HEAD"), cancellable = true)
	private void iris$fixGenericAttributes(int index, VertexFormatElement.Type type, CallbackInfoReturnable<Boolean> cir) {
		if (type == VertexFormatElement.Type.GENERIC) {
			cir.setReturnValue(true);
		}
	}
}
