package net.coderbot.iris.mixin;

import net.coderbot.iris.layer.EntityColorRenderStateShard;
import net.coderbot.iris.layer.InnerWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource_WrapperChecking {
	@ModifyVariable(method = "getBuffer", at = @At("HEAD"))
	private RenderType unwrapBufferIfNeeded(RenderType renderType) {
		// Ensure that entity color wrapped render layers do not take effect when entity batching is inoperable.
		if (renderType instanceof InnerWrappedRenderType) {
			if (((InnerWrappedRenderType) renderType).getExtra() instanceof EntityColorRenderStateShard) {
				return ((InnerWrappedRenderType) renderType).unwrap();
			}
		}

		return renderType;
	}
}
