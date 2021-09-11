package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public class OldFullyBufferedVertexConsumerProvider extends MultiBufferSource.BufferSource {
	private final Map<RenderType, BufferBuilder> bufferBuilders;
	private final Object2IntMap<RenderType> unused;
	private final Set<BufferBuilder> activeBuffers;
	private boolean flushed;

	private final Set<RenderType> layersThisFrame;
	private final List<RenderType> layersInOrder;

	public OldFullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.bufferBuilders = new HashMap<>();
		this.unused = new Object2IntOpenHashMap<>();
		this.activeBuffers = new HashSet<>();
		this.flushed = false;

		this.layersThisFrame = new HashSet<>();
		this.layersInOrder = new ArrayList<>();
	}

	private TransparencyType getTransparencyType(RenderType layer) {
		while (layer instanceof WrappableRenderLayer) {
			layer = ((WrappableRenderLayer) layer).unwrap();
		}

		if (layer instanceof BlendingStateHolder) {
			return ((BlendingStateHolder) layer).getTransparencyType();
		}

		// Default to "generally transparent" if we can't figure it out.
		return TransparencyType.GENERAL_TRANSPARENT;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderLayer) {
		flushed = false;

		BufferBuilder buffer = bufferBuilders.computeIfAbsent(renderLayer, layer -> new BufferBuilder(layer.bufferSize()));

		if (activeBuffers.add(buffer)) {
			buffer.begin(renderLayer.mode(), renderLayer.format());
		}

		if (this.layersThisFrame.add(renderLayer)) {
			// If we haven't seen this layer yet, add it to the list of layers to render.
			//
			// We keep track of the order that layers were added, in order to ensure that if layers are not
			// sorted relative each other due to translucency, that they are sorted in the order that they were
			// drawn in.
			//
			// This is important for things like villager rendering, where the clothes and skin of villagers overlap
			// each other, so if the clothes are drawn before the skin, they appear to be poorly-clothed.
			this.layersInOrder.add(renderLayer);
		}

		// If this buffer is scheduled to be removed, unschedule it since it's now being used.
		unused.removeInt(renderLayer);

		return buffer;
	}

	@Override
	public void endBatch() {
		if (flushed) {
			return;
		}

		List<RenderType> removedLayers = new ArrayList<>();

		unused.forEach((unusedLayer, unusedCount) -> {
			if (unusedCount < 10) {
				// Removed after 10 frames of not being used
				return;
			}

			BufferBuilder buffer = bufferBuilders.remove(unusedLayer);
			removedLayers.add(unusedLayer);

			if (activeBuffers.contains(buffer)) {
				throw new IllegalStateException(
						"A buffer was simultaneously marked as inactive and as active, something is very wrong...");
			}
		});

		for (RenderType removed : removedLayers) {
			unused.removeInt(removed);
		}

		// Make sure translucent layers are rendered after non-translucent ones.
		layersInOrder.sort(Comparator.comparing(this::getTransparencyType));

		for (RenderType layer : layersInOrder) {
			drawInternal(layer);
		}

		layersInOrder.clear();
		layersThisFrame.clear();

		flushed = true;
	}

	@Override
	public void endBatch(RenderType layer) {
		// Disable explicit flushing
	}

	private void drawInternal(RenderType layer) {
		BufferBuilder buffer = bufferBuilders.get(layer);

		if (buffer == null) {
			return;
		}

		if (activeBuffers.remove(buffer)) {
			layer.end(buffer, 0, 0, 0);
			buffer.clear();
		} else {
			// Schedule the buffer for removal next frame if it isn't used this frame.
			int unusedCount = unused.getOrDefault(layer, 0);

			unusedCount += 1;

			unused.put(layer, unusedCount);
		}
	}
}
