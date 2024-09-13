package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.irisshaders.batchedentityrendering.impl.ordering.GraphTranslucencyRenderOrderManager;
import net.irisshaders.batchedentityrendering.impl.ordering.RenderOrderManager;
import net.irisshaders.iris.layer.WrappingMultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FullyBufferedMultiBufferSource extends MultiBufferSource.BufferSource implements MemoryTrackingBuffer, Groupable, WrappingMultiBufferSource {
	private static final int NUM_BUFFERS = 32;

	private final RenderOrderManager renderOrderManager;
	private final SegmentedBufferBuilder[] builders;
	/**
	 * An LRU cache mapping RenderType objects to a relevant buffer.
	 */
	private final LinkedHashMap<RenderType, Integer> affinities;
	private final BufferSegmentRenderer segmentRenderer;
	private final UnflushableWrapper unflushableWrapper;
	private final List<Function<RenderType, RenderType>> wrappingFunctionStack;
	private final Map<RenderType, List<BufferSegment>> typeToSegment = new HashMap<>();
	private int drawCalls;
	private int renderTypes;
	private Function<RenderType, RenderType> wrappingFunction = null;
	private boolean isReady;
	private List<RenderType> renderOrder = new ArrayList<>();

	public FullyBufferedMultiBufferSource() {
		super(new ByteBufferBuilder(0), Object2ObjectSortedMaps.emptyMap());

		this.renderOrderManager = new GraphTranslucencyRenderOrderManager();
		this.builders = new SegmentedBufferBuilder[NUM_BUFFERS];

		for (int i = 0; i < this.builders.length; i++) {
			this.builders[i] = new SegmentedBufferBuilder(this);
		}

		// use accessOrder=true so our LinkedHashMap works as an LRU cache.
		this.affinities = new LinkedHashMap<>(32, 0.75F, true);

		this.drawCalls = 0;
		this.segmentRenderer = new BufferSegmentRenderer();
		this.unflushableWrapper = new UnflushableWrapper(this);
		this.wrappingFunctionStack = new ArrayList<>();
	}

	private static long toMib(long x) {
		return x / 1024L / 1024L;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		removeReady();

		if (wrappingFunction != null) {
			renderType = wrappingFunction.apply(renderType);
		}

		renderOrderManager.begin(renderType);
		Integer affinity = affinities.get(renderType);

		if (affinity == null) {
			if (affinities.size() < builders.length) {
				affinity = affinities.size();
			} else {
				// We remove the element from the map that is used least-frequently.
				// With how we've configured our LinkedHashMap, that is the first element.
				Iterator<Map.Entry<RenderType, Integer>> iterator = affinities.entrySet().iterator();
				Map.Entry<RenderType, Integer> evicted = iterator.next();
				iterator.remove();

				// The previous type is no longer associated with this buffer ...
				affinities.remove(evicted.getKey());

				// ... since our new type is now associated with it.
				affinity = evicted.getValue();
			}

			affinities.put(renderType, affinity);
		}

		return builders[affinity].getBuffer(renderType);
	}

	private void removeReady() {
		isReady = false;
		typeToSegment.clear();
		renderOrder.clear();
	}

	public void readyUp() {
		isReady = true;

		ProfilerFiller profiler = Profiler.get();

		profiler.push("iris collect");

		for (SegmentedBufferBuilder builder : builders) {
			List<BufferSegment> segments = builder.getSegments();

			for (BufferSegment segment : segments) {
				typeToSegment.computeIfAbsent(segment.type(), (type) -> new ArrayList<>()).add(segment);
			}
		}

		profiler.popPush("resolve ordering");

		renderOrder = renderOrderManager.getRenderOrder();

		renderOrderManager.reset();
		affinities.clear();

		profiler.pop();
	}

	@Override
	public void endBatch() {
		ProfilerFiller profiler = Profiler.get();

		if (!isReady) readyUp();

		profiler.push("iris draw buffers");

		for (RenderType type : renderOrder) {
			if (!typeToSegment.containsKey(type)) continue;

			type.setupRenderState();

			renderTypes += 1;

			for (BufferSegment segment : typeToSegment.getOrDefault(type, Collections.emptyList())) {
				segmentRenderer.drawInner(segment);
				drawCalls += 1;
			}

			type.clearRenderState();
		}

		int targetClearTime = getTargetClearTime();

		for (SegmentedBufferBuilder builder : builders) {
			builder.clearBuffers(targetClearTime);
		}

		profiler.popPush("reset");

		removeReady();

		profiler.pop();
	}

	public void endBatchWithType(TransparencyType transparencyType) {
		ProfilerFiller profiler = Profiler.get();

		if (!isReady) readyUp();

		profiler.push("iris draw partial");

		List<RenderType> types = new ArrayList<>();

		for (RenderType type : renderOrder) {
			if (((BlendingStateHolder) type).getTransparencyType() != transparencyType) {
				continue;
			}

			types.add(type);

			type.setupRenderState();

			renderTypes += 1;

			for (BufferSegment segment : typeToSegment.getOrDefault(type, Collections.emptyList())) {
				segmentRenderer.drawInner(segment);
				drawCalls += 1;
			}

			typeToSegment.remove(type);

			type.clearRenderState();
		}

		profiler.popPush("reset type " + transparencyType);

		renderOrder.removeAll(types);

		profiler.pop();
	}

	private int getTargetClearTime() {
		long sizeInMiB = toMib(getAllocatedSize());

		if (sizeInMiB > 5000) { // Over 5GB of RAM used.
			return 1000; // Be extremely aggressive; 1 second per buffer.
		} else if (sizeInMiB > 1000) { // Over 1GB of RAM used.
			return 5000; // Wait 5 seconds.
		} else {
			return 10000; // we chillin; 10 seconds.
		}
	}

	public int getDrawCalls() {
		return drawCalls;
	}

	public int getRenderTypes() {
		return renderTypes;
	}

	public void resetDrawCalls() {
		drawCalls = 0;
		renderTypes = 0;
	}

	@Override
	public void endBatch(RenderType type) {
		// Disable explicit flushing
	}

	public MultiBufferSource.BufferSource getUnflushableWrapper() {
		return unflushableWrapper;
	}

	@Override
	public long getAllocatedSize() {
		long size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getAllocatedSize();
		}

		return size;
	}

	@Override
	public long getUsedSize() {
		long size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getUsedSize();
		}

		return size;
	}

	@Override
	public void freeAndDeleteBuffer() {
		for (SegmentedBufferBuilder builder : builders) {
			builder.freeAndDeleteBuffer();
		}
	}

	@Override
	public void startGroup() {
		renderOrderManager.startGroup();
	}

	@Override
	public boolean maybeStartGroup() {
		return renderOrderManager.maybeStartGroup();
	}

	@Override
	public void endGroup() {
		renderOrderManager.endGroup();
	}

	@Override
	public void pushWrappingFunction(Function<RenderType, RenderType> wrappingFunction) {
		if (this.wrappingFunction != null) {
			this.wrappingFunctionStack.add(this.wrappingFunction);
		}

		this.wrappingFunction = wrappingFunction;
	}

	@Override
	public void popWrappingFunction() {
		if (this.wrappingFunctionStack.isEmpty()) {
			this.wrappingFunction = null;
		} else {
			this.wrappingFunction = this.wrappingFunctionStack.removeLast();
		}
	}

	@Override
	public void assertWrapStackEmpty() {
		if (!this.wrappingFunctionStack.isEmpty() || this.wrappingFunction != null) {
			throw new IllegalStateException("Wrapping function stack not empty!");
		}
	}

	public void weAreOutOfMemory() {
		for (SegmentedBufferBuilder builder : builders) {
			builder.lastDitchAttempt();
		}
	}

	/**
	 * A wrapper that prevents callers from explicitly flushing anything.
	 */
	private static class UnflushableWrapper extends MultiBufferSource.BufferSource implements Groupable {
		private final FullyBufferedMultiBufferSource wrapped;

		UnflushableWrapper(FullyBufferedMultiBufferSource wrapped) {
			super(new ByteBufferBuilder(0), Object2ObjectSortedMaps.emptyMap());

			this.wrapped = wrapped;
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			return wrapped.getBuffer(renderType);
		}

		@Override
		public void endBatch() {
			// Disable explicit flushing
		}

		@Override
		public void endBatch(RenderType type) {
			// Disable explicit flushing
		}

		@Override
		public void startGroup() {
			wrapped.startGroup();
		}

		@Override
		public boolean maybeStartGroup() {
			return wrapped.maybeStartGroup();
		}

		@Override
		public void endGroup() {
			wrapped.endGroup();
		}
	}
}
