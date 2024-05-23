package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.irisshaders.batchedentityrendering.impl.MemoryTrackingBuffer;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(ByteBufferBuilder.class)
public abstract class MixinByteBufferBuilder implements MemoryTrackingBuffer {
	@Shadow
	private int capacity;

	@Shadow
	private int writeOffset;

	@Shadow
	public abstract void close();

	@Override
	public int getAllocatedSize() {
		return this.capacity;
	}

	@Override
	public int getUsedSize() {
		return this.writeOffset;
	}

	@Override
	public void freeAndDeleteBuffer() {
		this.close();
	}
}
