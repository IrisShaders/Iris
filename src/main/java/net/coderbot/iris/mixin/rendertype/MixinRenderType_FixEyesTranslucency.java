package net.coderbot.iris.mixin.rendertype;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinRenderType_FixEyesTranslucency {
	@Redirect(method = "lambda$static$15(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "FIELD",
		target = "net/minecraft/client/renderer/RenderType.ADDITIVE_TRANSPARENCY : Lnet/minecraft/client/renderer/RenderStateShard$TransparencyStateShard;"))
	private static RenderStateShard.TransparencyStateShard iris$fixEyesTranslucency() {
		return RenderStateShardAccessor.getTranslucentTransparency();
	}
}
