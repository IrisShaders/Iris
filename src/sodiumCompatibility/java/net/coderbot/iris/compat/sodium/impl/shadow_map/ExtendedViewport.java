package net.coderbot.iris.compat.sodium.impl.shadow_map;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.joml.FrustumIntersection;

public class ExtendedViewport extends Viewport {
	private IrisFrustum frustum;

	public ExtendedViewport(IrisFrustum frustum, float x, float y, float z) {
		super(new FrustumIntersection[]{}, x,y,z);
		this.frustum = frustum;
	}

	@Override
	public boolean isBoxVisible(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return frustum.apply(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
