package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderType.class)
public class MixinRenderType implements BlendingStateHolder {
	// Fallback
	@Override
	public TransparencyType getTransparencyType() {
		return TransparencyType.GENERAL_TRANSPARENT;
	}
}
