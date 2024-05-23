package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

public class ByteBufferBuilderHolder implements MemoryTrackingBuffer {
	private final ByteBufferBuilder builder;
	private long lastUse;

	public ByteBufferBuilderHolder(ByteBufferBuilder builder) {
		this.lastUse = System.currentTimeMillis();
		this.builder = builder;
	}

	public ByteBufferBuilder getBuffer() {
		this.lastUse = System.currentTimeMillis();

		return builder;
	}

	public boolean deleteOrClear() {
		// If it's been 10 seconds since the last use, delete the buffer.
		if (lastUse - System.currentTimeMillis() > 10000) {
			this.builder.close();
			return true;
		} else {
			this.builder.clear();
			return false;
		}
	}

	public void forceDelete() {
		this.builder.close();
	}

	@Override
	public int getAllocatedSize() {
		return ((MemoryTrackingBuffer) builder).getAllocatedSize();
	}

	@Override
	public int getUsedSize() {
		return ((MemoryTrackingBuffer) builder).getUsedSize();
	}

	@Override
	public void freeAndDeleteBuffer() {
		builder.close();
	}

	public void wasUsed() {
		this.lastUse = System.currentTimeMillis();
	}
}
