package net.irisshaders.iris.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(remap = false, value = RenderSection.class)
public class MixinRenderSection {
	@Unique
	private int lastVisibleFrameShadow;


}
