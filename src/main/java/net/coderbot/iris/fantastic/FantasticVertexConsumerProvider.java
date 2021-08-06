package net.coderbot.iris.fantastic;

import net.coderbot.iris.layer.IrisRenderTypeWrapper;
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

	private boolean isTransparent(RenderType type) {
		if (type == RenderType.waterMask()) {
			// Don't break boats...
			return true;
		}

		if (type instanceof IrisRenderTypeWrapper) {
			IrisRenderTypeWrapper wrapped = (IrisRenderTypeWrapper) type;
			type = wrapped.unwrap();
		}

		if (type instanceof BlendingStateHolder) {
			return ((BlendingStateHolder) type).getTransparencyType() != TransparencyType.OPAQUE;
		}

		return true;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		if (isTransparent(renderType)) {
			return transparent.getBuffer(renderType);
		} else {
			return opaque.getBuffer(renderType);
		}
	}

	@Override
	public void endBatch() {
		opaque.endBatch();
		transparent.endBatch();
	}

	@Override
	public void endBatch(RenderType type) {
		if (isTransparent(type)) {
			transparent.endBatch(type);
		} else {
			opaque.endBatch(type);
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
