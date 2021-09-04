package net.coderbot.iris.shadows.frustum.fallback;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import net.coderbot.iris.shadows.frustum.SodiumFrustumExt;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class NonCullingFrustum extends Frustum implements SodiumFrustumExt {
	public NonCullingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	public boolean isVisible(Box box) {
		return true;
	}

	@Override
	public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return RenderRegionVisibility.FULLY_VISIBLE;
	}
}
