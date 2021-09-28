package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

import java.nio.ByteBuffer;

public class BufferSegment {
    private final ByteBuffer slice;
    private final BufferBuilder.DrawState drawState;
    private final RenderType type;

    public BufferSegment(ByteBuffer slice, BufferBuilder.DrawState drawState, RenderType type) {
        this.slice = slice;
        this.drawState = drawState;
        this.type = type;
    }

    public ByteBuffer getSlice() {
        return slice;
    }

    public BufferBuilder.DrawState getDrawState() {
        return drawState;
    }

    public RenderType getRenderType() {
        return type;
    }
}
