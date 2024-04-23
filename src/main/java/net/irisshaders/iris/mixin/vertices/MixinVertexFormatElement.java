package net.irisshaders.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes generic vertex attributes, previously it was only possible to use a vertex attribute with a location of 0.
 */
@Mixin(VertexFormatElement.class)
public class MixinVertexFormatElement {
	@Inject(method = "supportsUsage", at = @At("HEAD"), cancellable = true)
	private void iris$fixGenericAttributes(int index, VertexFormatElement.Usage type, CallbackInfoReturnable<Boolean> cir) {
		if (type == VertexFormatElement.Usage.GENERIC || type == VertexFormatElement.Usage.PADDING) {
			cir.setReturnValue(true);
		}
	}
}
