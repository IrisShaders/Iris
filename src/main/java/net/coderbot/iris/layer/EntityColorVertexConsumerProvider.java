package net.coderbot.iris.layer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Collections;

public class EntityColorVertexConsumerProvider extends MultiBufferSource.BufferSource implements Groupable {
	private final MultiBufferSource wrapped;
	private final MultiBufferSource.BufferSource wrappedImmediate;
	private final Groupable groupable;
	private final EntityColorRenderStateShard phase;

	public EntityColorVertexConsumerProvider(MultiBufferSource wrapped, EntityColorRenderStateShard phase) {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.wrapped = wrapped;

		if (wrapped instanceof BufferSource) {
			this.wrappedImmediate = (BufferSource) wrapped;
		} else {
			this.wrappedImmediate = null;
		}

		if (wrapped instanceof Groupable) {
			this.groupable = (Groupable) wrapped;
		} else {
			this.groupable = null;
		}

		this.phase = phase;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderLayer) {
		return wrapped.getBuffer(new InnerWrappedRenderLayer("iris_entity_color", renderLayer, phase));
	}

	@Override
	public void endBatch() {
		if (wrappedImmediate != null) {
			wrappedImmediate.endBatch();
		}
	}

	@Override
	public void endBatch(RenderType layer) {
		if (wrappedImmediate != null) {
			wrappedImmediate.endBatch(layer);
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
