package net.coderbot.iris.mixin;

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
