package net.coderbot.iris.layer;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.RenderStateShard;

public final class EntityRenderStateShard extends RenderStateShard {
	private static final EntityRenderStateShard UNIDENTIFIED = new EntityRenderStateShard(-1);

	private final int entityId;

	private EntityRenderStateShard(int entityId) {
		super("iris:is_entity", () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
			GbufferPrograms.beginEntities();
		}, () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
			GbufferPrograms.endEntities();
		});

		this.entityId = entityId;
	}

	public static EntityRenderStateShard forId(int entityId) {
		if (entityId == -1) {
			return UNIDENTIFIED;
		} else {
			// TODO: Cache all created render phases to avoid allocations?
			return new EntityRenderStateShard(entityId);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass()) {
			return false;
		}

		EntityRenderStateShard other = (EntityRenderStateShard) object;

		return this.entityId == other.entityId;
	}
}
