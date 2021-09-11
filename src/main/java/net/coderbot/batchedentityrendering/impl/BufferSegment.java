package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

import java.nio.ByteBuffer;

public class BufferSegment {
    private final ByteBuffer slice;
    private final BufferBuilder.DrawState parameters;
    private final RenderType layer;

    public BufferSegment(ByteBuffer slice, BufferBuilder.DrawState parameters, RenderType layer) {
        this.slice = slice;
        this.parameters = parameters;
        this.layer = layer;
    }

    public ByteBuffer getSlice() {
        return slice;
    }

    public BufferBuilder.DrawState getParameters() {
        return parameters;
    }

    public RenderType getRenderLayer() {
        return layer;
    }
}
