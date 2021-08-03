package net.coderbot.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public class IsEntityRenderPhase extends RenderStateShard {
	public static final IsEntityRenderPhase INSTANCE = new IsEntityRenderPhase();

	private IsEntityRenderPhase() {
		super("iris:is_entity", GbufferPrograms::beginEntities, GbufferPrograms::endEntities);
	}
}
