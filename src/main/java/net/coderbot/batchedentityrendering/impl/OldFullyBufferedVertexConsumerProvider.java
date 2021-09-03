package net.coderbot.batchedentityrendering.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OldFullyBufferedVertexConsumerProvider extends VertexConsumerProvider.Immediate {
	private final Map<RenderLayer, BufferBuilder> bufferBuilders;
	private final Object2IntMap<RenderLayer> unused;
	private final Set<BufferBuilder> activeBuffers;
	private boolean flushed;

	private final Set<RenderLayer> layersThisFrame;
	private final List<RenderLayer> layersInOrder;

	public OldFullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.bufferBuilders = new HashMap<>();
		this.unused = new Object2IntOpenHashMap<>();
		this.activeBuffers = new HashSet<>();
		this.flushed = false;

		this.layersThisFrame = new HashSet<>();
		this.layersInOrder = new ArrayList<>();
	}

	private TransparencyType getTransparencyType(RenderLayer layer) {
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
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		flushed = false;

		BufferBuilder buffer = bufferBuilders.computeIfAbsent(renderLayer, layer -> new BufferBuilder(layer.getExpectedBufferSize()));

		if (activeBuffers.add(buffer)) {
			buffer.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
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
	public void draw() {
		if (flushed) {
			return;
		}

		List<RenderLayer> removedLayers = new ArrayList<>();

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

		for (RenderLayer removed : removedLayers) {
			unused.removeInt(removed);
		}

		// Make sure translucent layers are rendered after non-translucent ones.
		layersInOrder.sort(Comparator.comparing(this::getTransparencyType));

		for (RenderLayer layer : layersInOrder) {
			drawInternal(layer);
		}

		layersInOrder.clear();
		layersThisFrame.clear();

		flushed = true;
	}

	@Override
	public void draw(RenderLayer layer) {
		// Disable explicit flushing
	}

	private void drawInternal(RenderLayer layer) {
		BufferBuilder buffer = bufferBuilders.get(layer);

		if (buffer == null) {
			return;
		}

		if (activeBuffers.remove(buffer)) {
			layer.draw(buffer, 0, 0, 0);
			buffer.reset();
		} else {
			// Schedule the buffer for removal next frame if it isn't used this frame.
			int unusedCount = unused.getOrDefault(layer, 0);

			unusedCount += 1;

			unused.put(layer, unusedCount);
		}
	}
}
