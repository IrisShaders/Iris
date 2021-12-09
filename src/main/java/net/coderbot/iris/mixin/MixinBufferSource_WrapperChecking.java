package net.coderbot.iris.mixin;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.EntityColorRenderStateShard;
import net.coderbot.iris.layer.InnerWrappedRenderType;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.mixin.rendertype.RenderStateShardAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource_WrapperChecking {
	@Unique
	private final Set<String> unwrapped = new ObjectOpenHashSet<>();

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

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"))
	private void iris$beginDraw(RenderType renderType, CallbackInfo ci) {
		if (!(renderType instanceof IrisRenderTypeWrapper) && !(renderType instanceof InnerWrappedRenderType)) {
			String name = ((RenderStateShardAccessor) renderType).getName();

			if (unwrapped.contains(name)) {
				return;
			}

			unwrapped.add(name);

			Iris.logger.warn("Iris has detected a non-wrapped render layer, it will not be rendered with the correct shader program: " + name);
		}
	}
}
