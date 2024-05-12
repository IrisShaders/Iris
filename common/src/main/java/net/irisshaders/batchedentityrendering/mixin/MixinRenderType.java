package net.irisshaders.batchedentityrendering.mixin;

import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderType.class)
public class MixinRenderType implements BlendingStateHolder {
	@Unique
	private TransparencyType transparencyType = TransparencyType.GENERAL_TRANSPARENT;

	// Fallback
	@Override
	public TransparencyType getTransparencyType() {
		return transparencyType;
	}

	@Override
	public void setTransparencyType(TransparencyType transparencyType) {
		this.transparencyType = transparencyType;
	}
}
