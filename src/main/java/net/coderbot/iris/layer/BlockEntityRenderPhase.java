package net.coderbot.iris.layer;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.RenderPhase;

public final class BlockEntityRenderPhase extends RenderPhase {
	private final int entityId;

	public BlockEntityRenderPhase(int entityId) {
		super("iris:is_block_entity", () -> {
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(entityId);
			GbufferPrograms.beginBlockEntities();
		}, () -> {
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(-1);
			GbufferPrograms.endBlockEntities();
		});

		this.entityId = entityId;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass()) {
			return false;
		}

		BlockEntityRenderPhase other = (BlockEntityRenderPhase) object;

		return this.entityId == other.entityId;
	}
}
