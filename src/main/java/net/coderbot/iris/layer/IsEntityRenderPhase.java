package net.coderbot.iris.layer;

import net.minecraft.client.render.RenderPhase;

public class IsEntityRenderPhase extends RenderPhase {
	public static final IsEntityRenderPhase INSTANCE = new IsEntityRenderPhase();

	private IsEntityRenderPhase() {
		super("iris:is_entity", GbufferPrograms::beginEntities, GbufferPrograms::endEntities);
	}
}
