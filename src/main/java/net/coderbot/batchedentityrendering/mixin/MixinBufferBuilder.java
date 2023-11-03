package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import org.lwjgl.system.MemoryUtil;
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

	@Override
	public void freeAndDeleteBuffer() {
		if (buffer == null) return;
		MemoryUtil.getAllocator(false).free(MemoryUtil.memAddress(buffer));
		buffer = null;
	}
}
