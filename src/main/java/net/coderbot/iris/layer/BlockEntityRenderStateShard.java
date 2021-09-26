package net.coderbot.iris.layer;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.RenderStateShard;

public final class BlockEntityRenderStateShard extends RenderStateShard {
	private static final BlockEntityRenderStateShard UNIDENTIFIED = new BlockEntityRenderStateShard(-1);

	private final int entityId;

	private BlockEntityRenderStateShard(int entityId) {
		super("iris:is_block_entity", () -> {
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(entityId);
			GbufferPrograms.beginBlockEntities();
		}, () -> {
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(-1);
			GbufferPrograms.endBlockEntities();
		});

		this.entityId = entityId;
	}

	public static BlockEntityRenderStateShard forId(int entityId) {
		if (entityId == -1) {
			return UNIDENTIFIED;
		} else {
			// TODO: Cache all created render phases to avoid allocations?
			return new BlockEntityRenderStateShard(entityId);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass()) {
			return false;
		}

		BlockEntityRenderStateShard other = (BlockEntityRenderStateShard) object;

		return this.entityId == other.entityId;
	}
}
