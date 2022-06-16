package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

import java.nio.ByteBuffer;

public class BufferSegment {
    private final BufferBuilder.RenderedBuffer renderedBuffer;
    private final RenderType type;

    public BufferSegment(BufferBuilder.RenderedBuffer renderedBuffer, RenderType type) {
		this.renderedBuffer = renderedBuffer;
        this.type = type;
    }

	public BufferBuilder.RenderedBuffer getRenderedBuffer() {
		return renderedBuffer;
	}

	public RenderType getRenderType() {
        return type;
    }
}
