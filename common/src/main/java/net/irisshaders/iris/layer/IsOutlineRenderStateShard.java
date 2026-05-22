package net.irisshaders.iris.layer;


public class IsOutlineRenderStateShard implements RenderingWrapper {
	public static final IsOutlineRenderStateShard INSTANCE = new IsOutlineRenderStateShard();

	private IsOutlineRenderStateShard() {
	}

	@Override
	public void setup() {
		GbufferPrograms.beginOutline();
	}

	@Override
	public void clear() {
		GbufferPrograms.endOutline();
	}
}
