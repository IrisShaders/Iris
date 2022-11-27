package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.coderbot.batchedentityrendering.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SegmentedBufferBuilder implements MultiBufferSource, MemoryTrackingBuffer {
    private final BufferBuilder buffer;
    private RenderType currentType;
	private final List<BufferSegment> buffers;

    public SegmentedBufferBuilder() {
        // 2 MB initial allocation
        this.buffer = new BufferBuilder(512 * 1024);
		this.buffers = new ArrayList<>();
        this.currentType = null;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (!Objects.equals(currentType, renderType)) {
            if (currentType != null) {
                if (shouldSortOnUpload(currentType)) {
                    buffer.setQuadSortOrigin(0, 0, 0);
                }

                buffers.add(new BufferSegment(Objects.requireNonNull(buffer.end()), currentType));
            }

            buffer.begin(renderType.mode(), renderType.format());

            currentType = renderType;
        }

        // Use duplicate vertices to break up triangle strips
        // https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/Art/degenerate_triangle_strip_2x.png
        // This works by generating zero-area triangles that don't end up getting rendered.
        // TODO: How do we handle DEBUG_LINE_STRIP?
        if (RenderTypeUtil.isTriangleStripDrawMode(currentType)) {
            ((BufferBuilderExt) buffer).splitStrip();
        }

        return buffer;
    }

    public List<BufferSegment> getSegments() {
        if (currentType == null) {
            return Collections.emptyList();
        }

        if (shouldSortOnUpload(currentType)) {
            buffer.setQuadSortOrigin(0, 0, 0);
        }

		buffers.add(new BufferSegment(Objects.requireNonNull(buffer.end()), currentType));

		currentType = null;

		List<BufferSegment> finalSegments = new ArrayList<>(buffers);

		buffers.clear();

        return finalSegments;
    }

    private static boolean shouldSortOnUpload(RenderType type) {
        return ((RenderTypeAccessor) type).shouldSortOnUpload();
    }

    @Override
    public int getAllocatedSize() {
        return ((MemoryTrackingBuffer) buffer).getAllocatedSize();
    }

    @Override
    public int getUsedSize() {
        return ((MemoryTrackingBuffer) buffer).getUsedSize();
    }

	@Override
	public void freeAndDeleteBuffer() {
		((MemoryTrackingBuffer) buffer).freeAndDeleteBuffer();
	}
}
