package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.MemoryTrackingBuffer;
import net.minecraft.client.render.BufferBuilder;
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
