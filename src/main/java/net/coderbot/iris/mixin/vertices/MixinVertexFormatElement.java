package net.coderbot.iris.mixin.vertices;

import net.minecraft.client.render.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexFormatElement.class)
public class MixinVertexFormatElement {
	@Inject(method = "isValidType(ILnet/minecraft/client/render/VertexFormatElement$Type;)Z", at = @At("HEAD"), cancellable = true)
	private void iris$fixGenericAttributes(int index, VertexFormatElement.Type type, CallbackInfoReturnable<Boolean> cir) {
		if (type == VertexFormatElement.Type.GENERIC) {
			cir.setReturnValue(true);
		}
	}
}
