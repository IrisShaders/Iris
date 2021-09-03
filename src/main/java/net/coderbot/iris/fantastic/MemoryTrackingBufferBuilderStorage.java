package net.coderbot.iris.fantastic;

public interface MemoryTrackingBufferBuilderStorage {
    int getEntityBufferAllocatedSize();
    int getMiscBufferAllocatedSize();
    int getMaxBegins();
}
