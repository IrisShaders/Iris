package net.coderbot.iris.shadows.frustum;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;

public interface SodiumFrustumExt {
	RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
}
