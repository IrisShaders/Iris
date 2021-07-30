package net.coderbot.iris.fantastic;

import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.Collections;

public class FantasticVertexConsumerProvider extends VertexConsumerProvider.Immediate implements FlushableVertexConsumerProvider {
	private final FullyBufferedVertexConsumerProvider opaque;
	private final FullyBufferedVertexConsumerProvider transparent;

	public FantasticVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.opaque = new FullyBufferedVertexConsumerProvider();
		this.transparent = new FullyBufferedVertexConsumerProvider();
	}

	private boolean isTransparent(RenderLayer layer) {
		if (layer == RenderLayer.getWaterMask()) {
			// Don't break boats...
			return true;
		}

		if (layer instanceof IrisRenderLayerWrapper) {
			IrisRenderLayerWrapper wrapped = (IrisRenderLayerWrapper) layer;
			layer = wrapped.unwrap();
		}

		if (layer instanceof BlendingStateHolder) {
			return ((BlendingStateHolder) layer).getTransparencyType() != TransparencyType.OPAQUE;
		}

		return true;
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		if (isTransparent(renderLayer)) {
			return transparent.getBuffer(renderLayer);
		} else {
			return opaque.getBuffer(renderLayer);
		}
	}

	@Override
	public void draw() {
		opaque.draw();
		transparent.draw();
	}

	@Override
	public void draw(RenderLayer layer) {
		if (isTransparent(layer)) {
			transparent.draw(layer);
		} else {
			opaque.draw(layer);
		}
	}

	@Override
	public void flushNonTranslucentContent() {
		opaque.draw();
	}

	@Override
	public void flushTranslucentContent() {
		transparent.draw();
	}
}
