package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferUploader;

public class BufferSegmentRenderer {
    public BufferSegmentRenderer() {
    }

    /**
     * Sets up the render type, draws the buffer, and then tears down the render type.
     */
    public void draw(BufferSegment segment) {
		if (segment.getRenderedBuffer() != null) {
			segment.getRenderType().setupRenderState();
			drawInner(segment);
			segment.getRenderType().clearRenderState();
		}
    }

    /**
     * Like draw(), but it doesn't setup / tear down the render type.
     */
    public void drawInner(BufferSegment segment) {
		BufferUploader.drawWithShader(segment.getRenderedBuffer());
	}
}
