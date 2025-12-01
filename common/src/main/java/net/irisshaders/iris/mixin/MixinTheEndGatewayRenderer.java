package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndGatewayRenderer.class)
public class MixinTheEndGatewayRenderer {
	@Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
	private static void iris$renderType(CallbackInfoReturnable<RenderType> cir) {
		if (Iris.getCurrentPack().isPresent()) {
			cir.setReturnValue(RenderType.entitySolid(TheEndPortalRenderer.END_PORTAL_LOCATION));
		}
	}
}
