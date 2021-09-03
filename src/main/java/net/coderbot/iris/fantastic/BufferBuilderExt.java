package net.coderbot.iris.fantastic;

import net.minecraft.client.render.BufferBuilder;

import java.nio.ByteBuffer;

public interface BufferBuilderExt {
    void setupBufferSlice(ByteBuffer buffer, BufferBuilder.DrawArrayParameters parameters);
    void teardownBufferSlice();
    void splitStrip();
}
