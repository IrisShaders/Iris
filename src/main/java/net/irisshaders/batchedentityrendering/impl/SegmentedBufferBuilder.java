package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.batchedentityrendering.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SegmentedBufferBuilder implements MultiBufferSource, MemoryTrackingBuffer {
	private final ByteBufferBuilder buffer;
	private final Map<RenderType, BufferBuilder> builders;
	private final List<BufferSegment> buffers;

	public SegmentedBufferBuilder() {
		// 2 MB initial allocation
		this.buffer = new ByteBufferBuilder(512 * 1024);
		this.builders = new Object2ObjectOpenHashMap<>();
		this.buffers = new ArrayList<>();
	}

	private static boolean shouldSortOnUpload(RenderType type) {
		return ((RenderTypeAccessor) type).shouldSortOnUpload();
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		BufferBuilder builder = builders.computeIfAbsent(renderType, (t) -> new BufferBuilder(buffer, renderType.mode(), renderType.format()));

		// Use duplicate vertices to break up triangle strips
		// https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/Art/degenerate_triangle_strip_2x.png
		// This works by generating zero-area triangles that don't end up getting rendered.
		// TODO: How do we handle DEBUG_LINE_STRIP?
		if (RenderTypeUtil.isTriangleStripDrawMode(renderType)) {
			((BufferBuilderExt) builder).splitStrip();
		}

		return builder;
	}

	public List<BufferSegment> getSegments() {
		builders.forEach(((renderType, bufferBuilder) -> {
			MeshData meshData = bufferBuilder.build();

			if (meshData == null) return;

			if (shouldSortOnUpload(renderType)) {
				meshData.sortQuads(buffer, RenderSystem.getVertexSorting());
			}

			buffers.add(new BufferSegment(meshData, renderType));
		}));

		builders.clear();

		List<BufferSegment> finalSegments = new ArrayList<>(buffers);

		buffers.clear();

		return finalSegments;
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
