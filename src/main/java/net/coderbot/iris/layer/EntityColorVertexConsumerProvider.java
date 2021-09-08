package net.coderbot.iris.layer;

import net.coderbot.batchedentityrendering.impl.Groupable;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.Collections;

public class EntityColorVertexConsumerProvider extends VertexConsumerProvider.Immediate implements Groupable {
	private final VertexConsumerProvider wrapped;
	private final VertexConsumerProvider.Immediate wrappedImmediate;
	private final Groupable groupable;
	private final EntityColorRenderPhase phase;

	public EntityColorVertexConsumerProvider(VertexConsumerProvider wrapped, EntityColorRenderPhase phase) {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.wrapped = wrapped;

		if (wrapped instanceof Immediate) {
			this.wrappedImmediate = (Immediate) wrapped;
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
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		return wrapped.getBuffer(new InnerWrappedRenderLayer("iris_entity_color", renderLayer, phase));
	}

	@Override
	public void draw() {
		if (wrappedImmediate != null) {
			wrappedImmediate.draw();
		}
	}

	@Override
	public void draw(RenderLayer layer) {
		if (wrappedImmediate != null) {
			wrappedImmediate.draw(layer);
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
