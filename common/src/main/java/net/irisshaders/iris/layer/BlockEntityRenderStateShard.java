package net.irisshaders.iris.layer;

public final class BlockEntityRenderStateShard implements RenderingWrapper {
	public static final BlockEntityRenderStateShard INSTANCE = new BlockEntityRenderStateShard();

	private BlockEntityRenderStateShard() {
	}

	@Override
	public void setup() {
		GbufferPrograms.beginBlockEntities();
	}

	@Override
	public void clear() {
		GbufferPrograms.endBlockEntities();
	}
}
