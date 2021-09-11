package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;

public class BufferSegmentRenderer {
    private final BufferBuilder fakeBufferBuilder;
    private final BufferBuilderExt fakeBufferBuilderExt;

    public BufferSegmentRenderer() {
        this.fakeBufferBuilder = new BufferBuilder(0);
        this.fakeBufferBuilderExt = (BufferBuilderExt) this.fakeBufferBuilder;
    }

    /**
     * Sets up the render layer, draws the buffer, and then tears down the render layer.
     */
    public void draw(BufferSegment segment) {
        segment.getRenderLayer().startDrawing();
        drawInner(segment);
        segment.getRenderLayer().endDrawing();
    }

    /**
     * Like draw(), but it doesn't setup / tear down the render layer.
     */
    public void drawInner(BufferSegment segment) {
        fakeBufferBuilderExt.setupBufferSlice(segment.getSlice(), segment.getParameters());
        BufferRenderer.draw(fakeBufferBuilder);
        fakeBufferBuilderExt.teardownBufferSlice();
    }
}
