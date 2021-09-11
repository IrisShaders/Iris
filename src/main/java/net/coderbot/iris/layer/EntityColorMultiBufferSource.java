package net.coderbot.iris.layer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Collections;

public class EntityColorMultiBufferSource extends MultiBufferSource.BufferSource implements Groupable {
	private final MultiBufferSource wrapped;
	private final MultiBufferSource.BufferSource wrappedBufferSource;
	private final Groupable groupable;
	private final EntityColorRenderStateShard phase;

	public EntityColorMultiBufferSource(MultiBufferSource wrapped, EntityColorRenderStateShard phase) {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.wrapped = wrapped;

		if (wrapped instanceof BufferSource) {
			this.wrappedBufferSource = (BufferSource) wrapped;
		} else {
			this.wrappedBufferSource = null;
		}

		if (wrapped instanceof Groupable) {
			this.groupable = (Groupable) wrapped;
		} else {
			this.groupable = null;
		}

		this.phase = phase;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return wrapped.getBuffer(new InnerWrappedRenderType("iris_entity_color", renderType, phase));
	}

	@Override
	public void endBatch() {
		if (wrappedBufferSource != null) {
			wrappedBufferSource.endBatch();
		}
	}

	@Override
	public void endBatch(RenderType type) {
		if (wrappedBufferSource != null) {
			wrappedBufferSource.endBatch(type);
		}
	}

	@Override
	public void startGroup() {
		if (groupable != null) {
			groupable.startGroup();
		}
	}

	@Override
	public boolean maybeStartGroup() {
		if (groupable != null) {
			return groupable.maybeStartGroup();
		}

		return false;
	}

	@Override
	public void endGroup() {
		if (groupable != null) {
			groupable.endGroup();
		}
	}
}
