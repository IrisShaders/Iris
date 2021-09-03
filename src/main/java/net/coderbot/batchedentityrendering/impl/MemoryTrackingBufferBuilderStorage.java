package net.coderbot.batchedentityrendering.impl;

public interface MemoryTrackingBufferBuilderStorage {
    int getEntityBufferAllocatedSize();
    int getMiscBufferAllocatedSize();
    int getMaxBegins();
}
