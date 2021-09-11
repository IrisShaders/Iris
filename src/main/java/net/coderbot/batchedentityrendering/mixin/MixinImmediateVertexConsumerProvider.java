package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(VertexConsumerProvider.Immediate.class)
public class MixinImmediateVertexConsumerProvider implements MemoryTrackingBuffer {
	@Shadow
	@Final
	protected BufferBuilder fallbackBuffer;

	@Shadow
	@Final
	protected Map<RenderLayer, BufferBuilder> layerBuffers;

	@Override
	public int getAllocatedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) fallbackBuffer).getAllocatedSize();

		for (BufferBuilder builder : layerBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getAllocatedSize();
		}

		return allocatedSize;
	}

	@Override
	public int getUsedSize() {
		int allocatedSize = ((MemoryTrackingBuffer) fallbackBuffer).getUsedSize();

		for (BufferBuilder builder : layerBuffers.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getUsedSize();
		}

		return allocatedSize;
	}
}
