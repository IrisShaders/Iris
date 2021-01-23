package net.coderbot.iris.mixin.renderlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinFixEyesTranslucency {
	// Minecraft interprets an alpha value of zero as a signal to disable the alpha test.
	// However, we actually want to reject all nonzero alpha values.
	//
	// Thus, Float.MIN_VALUE allows us to use such a ridiculously tiny value (1.4E-45F) that it is for all intents and
	// purposes zero, except when it comes to Minecraft's hardcoded `alpha > 0.0` check. Otherwise, it works just fine
	// for the alpha test.
	@Unique
	private static final RenderPhase.Alpha REJECT_ZERO_ALPHA = new RenderPhase.Alpha(Float.MIN_VALUE);

	@Redirect(method = "getEyes", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;transparency(Lnet/minecraft/client/render/RenderPhase$Transparency;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;"))
	private static RenderLayer.MultiPhaseParameters.Builder iris$fixEyesTranslucency(RenderLayer.MultiPhaseParameters.Builder instance, RenderPhase.Transparency ignored) {
		return instance.transparency(RenderPhaseAccessor.getTranslucentTransparency()).alpha(REJECT_ZERO_ALPHA);
	}
}
