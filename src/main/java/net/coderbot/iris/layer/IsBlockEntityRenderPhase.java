package net.coderbot.iris.layer;

import net.minecraft.client.render.RenderPhase;

public class IsBlockEntityRenderPhase extends RenderPhase {
	public static final IsBlockEntityRenderPhase INSTANCE = new IsBlockEntityRenderPhase();

	private IsBlockEntityRenderPhase() {
		super("iris:is_block_entity", GbufferPrograms::beginBlockEntities, GbufferPrograms::endBlockEntities);
	}
}
