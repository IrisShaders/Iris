package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;

import java.nio.ByteBuffer;

public interface BufferBuilderExt {
    void setupBufferSlice(ByteBuffer buffer, BufferBuilder.DrawState drawState);
    void teardownBufferSlice();
    void splitStrip();
}
