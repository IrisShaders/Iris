package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.DrawCallTrackingBufferBuilderStorage;
import net.coderbot.batchedentityrendering.impl.ExtendedBufferStorage;
import net.coderbot.batchedentityrendering.impl.FullyBufferedVertexConsumerProvider;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBufferBuilderStorage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBuffers.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage, MemoryTrackingBufferBuilderStorage, DrawCallTrackingBufferBuilderStorage {
	@Unique
	private final FullyBufferedVertexConsumerProvider buffered = new FullyBufferedVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private int maxBegins = 0;

	@Unique
	private final OutlineBufferSource outlineVertexConsumers = new OutlineBufferSource(buffered);

	@Shadow
	@Final
	private MultiBufferSource.BufferSource bufferSource;

	@Inject(method = "bufferSource", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceEntityVertexConsumers(CallbackInfoReturnable<MultiBufferSource.BufferSource> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(buffered);
	}

	@Inject(method = "crumblingBufferSource", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceEffectVertexConsumers(CallbackInfoReturnable<MultiBufferSource.BufferSource> cir) {
		if (begins == 0) {
			return;
		}

		// NB: We can return the same VertexConsumerProvider here as long as the block entity and its breaking animation
		// use different render layers. This seems like a sound assumption to make. This only works with our fully
		// buffered vertex consumer provider - vanilla's Immediate cannot be used here since it would try to return the
		// same buffer for the block entity and its breaking animation in many cases.
		//
		// If anything goes wrong here, Vanilla *will* catch the "duplicate delegates" error, so
		// this shouldn't cause silent bugs.

		// Prevent vanilla from explicitly flushing the wrapper at the wrong time.
		cir.setReturnValue(buffered.getUnflushableWrapper());
	}

	@Inject(method = "outlineBufferSource", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineBufferSource> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(outlineVertexConsumers);
	}

	@Override
	public void beginLevelRendering() {
		if (begins == 0) {
			buffered.assertWrapStackEmpty();
		}

		begins += 1;

		maxBegins = Math.max(begins, maxBegins);
	}

	@Override
	public void endLevelRendering() {
		begins -= 1;

		if (begins == 0) {
			buffered.assertWrapStackEmpty();
		}
	}

	@Override
	public int getEntityBufferAllocatedSize() {
		return ((MemoryTrackingBuffer) buffered).getAllocatedSize();
	}

	@Override
	public int getMiscBufferAllocatedSize() {
		return ((MemoryTrackingBuffer) bufferSource).getAllocatedSize();
	}

	@Override
	public int getMaxBegins() {
		return maxBegins;
	}

	@Override
	public int getDrawCalls() {
		return buffered.getDrawCalls();
	}

	@Override
	public int getRenderTypes() {
		return buffered.getRenderTypes();
	}

	@Override
	public void resetDrawCounts() {
		buffered.resetDrawCalls();
	}
}
