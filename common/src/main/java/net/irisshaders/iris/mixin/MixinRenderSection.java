package net.irisshaders.iris.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(remap = false, value = RenderSection.class)
public class MixinRenderSection {
	@Unique
	private int lastVisibleFrameShadow;


}
