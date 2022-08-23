package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
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
	protected BufferBuilder builder;

	@Shadow
	@Final
	protected Map<RenderType, BufferBuilder> fixedBuffers;

	@Override
	public int getAllocatedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) builder).getAllocatedSize();

		for (BufferBuilder builder : fixedBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getAllocatedSize();
		}

		return allocatedSize;
	}

	@Override
	public int getUsedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) builder).getUsedSize();

		for (BufferBuilder builder : fixedBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getUsedSize();
		}

		return allocatedSize;
	}

	@Override
	public void freeAndDeleteBuffer() {
		((MemoryTrackingBuffer) builder).freeAndDeleteBuffer();

		for (BufferBuilder builder : fixedBuffers.values()) {
			((MemoryTrackingBuffer) builder).freeAndDeleteBuffer();
		}
	}
}
