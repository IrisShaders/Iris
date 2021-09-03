package net.coderbot.iris.layer;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.RenderPhase;

public final class EntityRenderPhase extends RenderPhase {
	private final int entityId;

	public EntityRenderPhase(int entityId) {
		super("iris:is_entity", () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
			GbufferPrograms.push(GbufferProgram.ENTITIES);
		}, () -> {
			CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
			GbufferPrograms.pop(GbufferProgram.ENTITIES);
		});

		this.entityId = entityId;
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
