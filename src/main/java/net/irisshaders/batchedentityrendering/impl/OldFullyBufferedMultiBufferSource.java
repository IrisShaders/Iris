package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OldFullyBufferedMultiBufferSource extends MultiBufferSource.BufferSource {
	private final Map<RenderType, BufferBuilder> bufferBuilders;
	private final Object2IntMap<RenderType> unused;
	private final Set<BufferBuilder> activeBuffers;
	private final Set<RenderType> typesThisFrame;
	private final List<RenderType> typesInOrder;
	private boolean flushed;

	public OldFullyBufferedMultiBufferSource() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.bufferBuilders = new HashMap<>();
		this.unused = new Object2IntOpenHashMap<>();
		this.activeBuffers = new HashSet<>();
		this.flushed = false;

		this.typesThisFrame = new HashSet<>();
		this.typesInOrder = new ArrayList<>();
	}

	private TransparencyType getTransparencyType(RenderType type) {
		while (type instanceof WrappableRenderType) {
			type = ((WrappableRenderType) type).unwrap();
		}

		if (type instanceof BlendingStateHolder) {
			return ((BlendingStateHolder) type).getTransparencyType();
		}

		// Default to "generally transparent" if we can't figure it out.
		return TransparencyType.GENERAL_TRANSPARENT;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		flushed = false;

		BufferBuilder buffer = bufferBuilders.computeIfAbsent(renderType, type -> new BufferBuilder(type.bufferSize()));

		if (activeBuffers.add(buffer)) {
			buffer.begin(renderType.mode(), renderType.format());
		}

		if (this.typesThisFrame.add(renderType)) {
			// If we haven't seen this type yet, add it to the list of types to render.
			//
			// We keep track of the order that types were added, in order to ensure that if layers are not
			// sorted relative each other due to translucency, that they are sorted in the order that they were
			// drawn in.
			//
			// This is important for things like villager rendering, where the clothes and skin of villagers overlap
			// each other, so if the clothes are drawn before the skin, they appear to be poorly-clothed.
			this.typesInOrder.add(renderType);
		}

		// If this buffer is scheduled to be removed, unschedule it since it's now being used.
		unused.removeInt(renderType);

		return buffer;
	}

	@Override
	public void endBatch() {
		if (flushed) {
			return;
		}

		List<RenderType> removedTypes = new ArrayList<>();

		unused.forEach((unusedType, unusedCount) -> {
			if (unusedCount < 10) {
				// Removed after 10 frames of not being used
				return;
			}

			BufferBuilder buffer = bufferBuilders.remove(unusedType);
			removedTypes.add(unusedType);

			if (activeBuffers.contains(buffer)) {
				throw new IllegalStateException(
					"A buffer was simultaneously marked as inactive and as active, something is very wrong...");
			}
		});

		for (RenderType removed : removedTypes) {
			unused.removeInt(removed);
		}

		// Make sure translucent types are rendered after non-translucent ones.
		typesInOrder.sort(Comparator.comparing(this::getTransparencyType));

		for (RenderType type : typesInOrder) {
			drawInternal(type);
		}

		typesInOrder.clear();
		typesThisFrame.clear();

		flushed = true;
	}

	@Override
	public void endBatch(RenderType type) {
		// Disable explicit flushing
	}

	private void drawInternal(RenderType type) {
		BufferBuilder buffer = bufferBuilders.get(type);

		if (buffer == null) {
			return;
		}

		if (activeBuffers.remove(buffer)) {
			type.end(buffer, VertexSorting.DISTANCE_TO_ORIGIN);
			buffer.clear();
		} else {
			// Schedule the buffer for removal next frame if it isn't used this frame.
			int unusedCount = unused.getOrDefault(type, 0);

			unusedCount += 1;

			unused.put(type, unusedCount);
		}
	}
}
