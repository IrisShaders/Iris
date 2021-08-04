package net.coderbot.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public class IsEntityRenderState extends RenderStateShard {
	public static final IsEntityRenderState INSTANCE = new IsEntityRenderState();

	private IsEntityRenderState() {
		super("iris:is_entity", GbufferPrograms::beginEntities, GbufferPrograms::endEntities);
	}
}
