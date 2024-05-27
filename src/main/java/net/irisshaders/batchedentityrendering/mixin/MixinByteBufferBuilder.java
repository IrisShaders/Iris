package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.irisshaders.batchedentityrendering.impl.MemoryTrackingBuffer;
import org.lwjgl.system.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ByteBufferBuilder.class)
public abstract class MixinByteBufferBuilder implements MemoryTrackingBuffer {
	@Shadow
	private int capacity;

	@Shadow
	private int writeOffset;

	@Shadow
	public abstract void close();

	@Override
	public long getAllocatedSize() {
		return this.capacity;
	}

	@Override
	public long getUsedSize() {
		return this.writeOffset;
	}

	@Override
	public void freeAndDeleteBuffer() {
		this.close();
	}
}
