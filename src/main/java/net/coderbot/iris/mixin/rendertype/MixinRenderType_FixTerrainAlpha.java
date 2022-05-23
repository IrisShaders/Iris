package net.coderbot.iris.mixin.rendertype;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinRenderType_FixTerrainAlpha {
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setAlphaState(Lnet/minecraft/client/renderer/RenderStateShard$AlphaStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
	private static RenderType.CompositeState.CompositeStateBuilder iris$tweakCutoutAlpha(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard.AlphaStateShard alpha) {
		// OptiFine makes CUTOUT and CUTOUT_MIPPED use an alpha test of 0.1 instead of 0.5.
		//
		// We must replicate this behavior or else there will be issues.
		//
		// This is required for both shaders to work properly, and for bettermipmaps to work properly. It's an odd
		// case of mipmap implementation details leaking all the way through to shader packs.
		return builder.setAlphaState(new RenderStateShard.AlphaStateShard(0.1F));
	}
}
