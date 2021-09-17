package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.coderbot.batchedentityrendering.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SegmentedBufferBuilder implements MultiBufferSource, MemoryTrackingBuffer {
    private final BufferBuilder buffer;
    private final List<RenderType> usedLayers;
    private RenderType currentLayer;

    public SegmentedBufferBuilder() {
        // 2 MB initial allocation
        this.buffer = new BufferBuilder(512 * 1024);
        this.usedLayers = new ArrayList<>(256);

        this.currentLayer = null;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderLayer) {
        if (!Objects.equals(currentLayer, renderLayer)) {
            if (currentLayer != null) {
                if (isTranslucent(currentLayer)) {
                    buffer.setQuadSortOrigin(0, 0, 0);
                }

                buffer.end();
                usedLayers.add(currentLayer);
            }

            buffer.begin(renderLayer.mode(), renderLayer.format());

            currentLayer = renderLayer;
        }

        // Use duplicate vertices to break up triangle strips
        // https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/Art/degenerate_triangle_strip_2x.png
        // This works by generating zero-area triangles that don't end up getting rendered.
        // TODO: How do we handle DEBUG_LINE_STRIP?
        if (RenderLayerUtil.isTriangleStripDrawMode(currentLayer)) {
            ((BufferBuilderExt) buffer).splitStrip();
        }

        return buffer;
    }

    public List<BufferSegment> getSegments() {
        if (currentLayer == null) {
            return Collections.emptyList();
        }

        usedLayers.add(currentLayer);

        if (isTranslucent(currentLayer)) {
            buffer.setQuadSortOrigin(0, 0, 0);
        }

        buffer.end();
        currentLayer = null;

        List<BufferSegment> segments = new ArrayList<>(usedLayers.size());

        for (RenderType layer : usedLayers) {
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = buffer.popNextBuffer();

            BufferBuilder.DrawState parameters = pair.getFirst();
            ByteBuffer slice = pair.getSecond();

            segments.add(new BufferSegment(slice, parameters, layer));
        }

        usedLayers.clear();

        return segments;
    }

    private static boolean isTranslucent(RenderType layer) {
        return ((RenderTypeAccessor) layer).shouldSortOnUpload();
    }

    @Override
    public int getAllocatedSize() {
        return ((MemoryTrackingBuffer) buffer).getAllocatedSize();
    }

    @Override
    public int getUsedSize() {
        return ((MemoryTrackingBuffer) buffer).getUsedSize();
    }
}
