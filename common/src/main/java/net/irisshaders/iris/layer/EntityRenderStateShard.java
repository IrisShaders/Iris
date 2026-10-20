package net.irisshaders.iris.layer;

public final class EntityRenderStateShard implements RenderingWrapper {
	public static final EntityRenderStateShard INSTANCE = new EntityRenderStateShard();

	@Override
	public void setup() {
		GbufferPrograms.beginEntities();
	}

	@Override
	public void clear() {
		GbufferPrograms.endEntities();
	}
}
