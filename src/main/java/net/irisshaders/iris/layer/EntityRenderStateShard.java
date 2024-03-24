package net.irisshaders.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public final class EntityRenderStateShard extends RenderStateShard {
	public static final EntityRenderStateShard INSTANCE = new EntityRenderStateShard();

	private EntityRenderStateShard() {
		super("iris:is_entity", GbufferPrograms::beginEntities, GbufferPrograms::endEntities);
	}
}
