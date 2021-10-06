package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.irisshaders.batchedentityrendering.impl.MemoryTrackingBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder implements MemoryTrackingBuffer {
	@Shadow
	private ByteBuffer buffer;

	@Override
	public int getAllocatedSize() {
		return buffer.capacity();
	}

	@Override
	public int getUsedSize() {
		return buffer.position();
	}
}
