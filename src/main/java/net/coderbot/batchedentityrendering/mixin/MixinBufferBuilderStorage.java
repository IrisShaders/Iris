package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.DrawCallTrackingBufferBuilderStorage;
import net.coderbot.batchedentityrendering.impl.ExtendedBufferStorage;
import net.coderbot.batchedentityrendering.impl.FullyBufferedVertexConsumerProvider;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBufferBuilderStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage, MemoryTrackingBufferBuilderStorage, DrawCallTrackingBufferBuilderStorage {
	@Unique
	private final FullyBufferedVertexConsumerProvider buffered = new FullyBufferedVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private int maxBegins = 0;

	@Unique
	private final OutlineVertexConsumerProvider outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);

	@Shadow
	@Final
	private VertexConsumerProvider.Immediate entityVertexConsumers;

	@Inject(method = "getEntityVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceEntityVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(buffered);
	}

	@Inject(method = "getEffectVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceEffectVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
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
		provider.setReturnValue(buffered.getUnflushableWrapper());
	}

	@Inject(method = "getOutlineVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineVertexConsumerProvider> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(outlineVertexConsumers);
	}

	@Override
	public void beginWorldRendering() {
		if (begins == 0) {
			buffered.assertWrapStackEmpty();
		}

		begins += 1;

		maxBegins = Math.max(begins, maxBegins);
	}

	@Override
	public void endWorldRendering() {
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
		return ((MemoryTrackingBuffer) entityVertexConsumers).getAllocatedSize();
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
