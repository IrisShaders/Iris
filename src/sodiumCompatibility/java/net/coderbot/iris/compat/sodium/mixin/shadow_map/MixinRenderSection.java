package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import net.coderbot.iris.compat.sodium.impl.shadow_map.RenderSectionExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderSection.class)
public class MixinRenderSection implements RenderSectionExt {
	@Unique
	private int frameShadow;

	@Override
	public int getPreviousFrameShadow() {
		return frameShadow;
	}

	@Override
	public void setPreviousFrameShadow(int frame) {
		frameShadow = frame;
	}
}
