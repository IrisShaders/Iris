package net.coderbot.iris.fantastic;

import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;

public class FantasticVertexConsumerProvider extends MultiBufferSource.BufferSource implements FlushableVertexConsumerProvider {
	private final FullyBufferedVertexConsumerProvider opaque;
	private final FullyBufferedVertexConsumerProvider transparent;

	public FantasticVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.opaque = new FullyBufferedVertexConsumerProvider();
		this.transparent = new FullyBufferedVertexConsumerProvider();
	}

	private boolean isTransparent(RenderType layer) {
		if (layer == RenderType.waterMask()) {
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
	public VertexConsumer getBuffer(RenderType renderLayer) {
		if (isTransparent(renderLayer)) {
			return transparent.getBuffer(renderLayer);
		} else {
			return opaque.getBuffer(renderLayer);
		}
	}

	@Override
	public void endBatch() {
		opaque.endBatch();
		transparent.endBatch();
	}

	@Override
	public void endBatch(RenderType layer) {
		if (isTransparent(layer)) {
			transparent.endBatch(layer);
		} else {
			opaque.endBatch(layer);
		}
	}

	@Override
	public void flushNonTranslucentContent() {
		opaque.endBatch();
	}

	@Override
	public void flushTranslucentContent() {
		transparent.endBatch();
	}
}
