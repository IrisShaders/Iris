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
import java.util.List;
import java.util.Map;

public class SegmentedBufferBuilder implements MultiBufferSource, MemoryTrackingBuffer {
	private final Map<RenderType, ByteBufferBuilderHolder> buffers;
	private final Map<RenderType, BufferBuilder> builders;
	private final List<BufferSegment> segments;

	public SegmentedBufferBuilder() {
		// 2 MB initial allocation
		this.buffers = new Object2ObjectOpenHashMap<>();
		this.builders = new Object2ObjectOpenHashMap<>();
		this.segments = new ArrayList<>();
	}

	private static boolean shouldSortOnUpload(RenderType type) {
		return ((RenderTypeAccessor) type).shouldSortOnUpload();
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		ByteBufferBuilderHolder buffer = buffers.computeIfAbsent(renderType, (r) -> new ByteBufferBuilderHolder(new ByteBufferBuilder(512*2024)));

		buffer.wasUsed();
		BufferBuilder builder = builders.computeIfAbsent(renderType, (t) -> new BufferBuilder(buffer.getBuffer(), renderType.mode(), renderType.format()));

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
				meshData.sortQuads(buffers.get(renderType).getBuffer(), RenderSystem.getVertexSorting());
			}

			segments.add(new BufferSegment(meshData, renderType));
		}));

		builders.clear();

		List<BufferSegment> finalSegments = new ArrayList<>(segments);

		segments.clear();

		return finalSegments;
	}

	@Override
	public int getAllocatedSize() {
		int usedSize = 0;
		for (ByteBufferBuilderHolder buffer : buffers.values()) {
			usedSize += ((MemoryTrackingBuffer) buffer).getAllocatedSize();
		}

		return usedSize;
	}

	@Override
	public int getUsedSize() {
		int usedSize = 0;
		for (ByteBufferBuilderHolder buffer : buffers.values()) {
			usedSize += ((MemoryTrackingBuffer) buffer).getUsedSize();
		}

		return usedSize;
	}

	@Override
	public void freeAndDeleteBuffer() {
		for (ByteBufferBuilderHolder buffer : buffers.values()) {
			buffer.forceDelete();
		}

		buffers.clear();
	}

	public void clearBuffers() {
		buffers.values().removeIf(ByteBufferBuilderHolder::deleteOrClear);
	}
}
