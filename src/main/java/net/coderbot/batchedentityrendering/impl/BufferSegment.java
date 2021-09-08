package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

import java.nio.ByteBuffer;

public class BufferSegment {
    private final ByteBuffer slice;
    private final BufferBuilder.DrawArrayParameters parameters;
    private final RenderLayer layer;

    public BufferSegment(ByteBuffer slice, BufferBuilder.DrawArrayParameters parameters, RenderLayer layer) {
        this.slice = slice;
        this.parameters = parameters;
        this.layer = layer;
    }

    public ByteBuffer getSlice() {
        return slice;
    }

    public BufferBuilder.DrawArrayParameters getParameters() {
        return parameters;
    }

    public RenderLayer getRenderLayer() {
        return layer;
    }
}
