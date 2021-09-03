package net.coderbot.iris.layer;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.RenderPhase;

public final class EntityRenderPhase extends RenderPhase {
	private static final EntityRenderPhase UNIDENTIFIED = new EntityRenderPhase(-1);

	private final int entityId;

	private EntityRenderPhase(int entityId) {
		super("iris:is_entity", () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
			GbufferPrograms.beginEntities();
		}, () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
			GbufferPrograms.endEntities();
		});

		this.entityId = entityId;
	}

	public static EntityRenderPhase forId(int entityId) {
		if (entityId == -1) {
			return UNIDENTIFIED;
		} else {
			// TODO: Cache all created render phases to avoid allocations?
			return new EntityRenderPhase(entityId);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass()) {
			return false;
		}

		EntityRenderPhase other = (EntityRenderPhase) object;

		return this.entityId == other.entityId;
	}
}
