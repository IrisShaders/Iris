package net.coderbot.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public class IsBlockEntityRenderPhase extends RenderStateShard {
	public static final IsBlockEntityRenderPhase INSTANCE = new IsBlockEntityRenderPhase();

	private IsBlockEntityRenderPhase() {
		super("iris:is_block_entity", GbufferPrograms::beginBlockEntities, GbufferPrograms::endBlockEntities);
	}
}
