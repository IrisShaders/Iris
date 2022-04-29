package net.coderbot.iris.mixin.rendertype;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinFixEyesTranslucency {
	// Minecraft interprets an alpha value of zero as a signal to disable the alpha test.
	// However, we actually want to reject all nonzero alpha values.
	//
	// Thus, Float.MIN_VALUE allows us to use such a ridiculously tiny value (1.4E-45F) that it is for all intents and
	// purposes zero, except when it comes to Minecraft's hardcoded `alpha > 0.0` check. Otherwise, it works just fine
	// for the alpha test.
	@Unique
	private static final RenderStateShard.AlphaStateShard REJECT_ZERO_ALPHA = new RenderStateShard.AlphaStateShard(Float.MIN_VALUE);

	@Redirect(method = "eyes", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setTransparencyState(Lnet/minecraft/client/renderer/RenderStateShard$TransparencyStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
	private static RenderType.CompositeState.CompositeStateBuilder iris$fixEyesTranslucency(RenderType.CompositeState.CompositeStateBuilder instance, RenderStateShard.TransparencyStateShard ignored) {
		return instance.setTransparencyState(RenderStateShardAccessor.getTranslucentTransparency()).setAlphaState(REJECT_ZERO_ALPHA);
	}
}
