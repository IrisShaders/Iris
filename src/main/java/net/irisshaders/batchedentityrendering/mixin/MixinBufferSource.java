package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.irisshaders.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource implements MemoryTrackingBuffer {
	@Shadow
	@Final
	protected Map<RenderType, ByteBufferBuilder> fixedBuffers;

	@Shadow
	@Final
	protected ByteBufferBuilder sharedBuffer;

	@Override
	public int getAllocatedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) sharedBuffer).getAllocatedSize();

		for (ByteBufferBuilder builder : fixedBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getAllocatedSize();
		}

		return allocatedSize;
	}

	@Override
	public int getUsedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) sharedBuffer).getUsedSize();

		for (ByteBufferBuilder builder : fixedBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getUsedSize();
		}

		return allocatedSize;
	}

	@Override
	public void freeAndDeleteBuffer() {
		((MemoryTrackingBuffer) sharedBuffer).freeAndDeleteBuffer();

		for (ByteBufferBuilder builder : fixedBuffers.values()) {
			((MemoryTrackingBuffer) builder).freeAndDeleteBuffer();
		}
	}
}
