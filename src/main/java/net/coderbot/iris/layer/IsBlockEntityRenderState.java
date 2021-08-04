package net.coderbot.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public class IsBlockEntityRenderState extends RenderStateShard {
	public static final IsBlockEntityRenderState INSTANCE = new IsBlockEntityRenderState();

	private IsBlockEntityRenderState() {
		super("iris:is_block_entity", GbufferPrograms::beginBlockEntities, GbufferPrograms::endBlockEntities);
	}
}
