package net.irisshaders.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;

public class IsOutlineRenderStateShard extends RenderStateShard {
	public static final IsOutlineRenderStateShard INSTANCE = new IsOutlineRenderStateShard();

	private IsOutlineRenderStateShard() {
		super("iris:is_outline", GbufferPrograms::beginOutline, GbufferPrograms::endOutline);
	}
}
