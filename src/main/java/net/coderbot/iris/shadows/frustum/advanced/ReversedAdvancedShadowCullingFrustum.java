package net.coderbot.iris.shadows.frustum.advanced;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ReversedAdvancedShadowCullingFrustum extends AdvancedShadowCullingFrustum {
	public ReversedAdvancedShadowCullingFrustum(Matrix4f playerView, Matrix4f playerProjection, Vector3f shadowLightVectorFromOrigin, BoxCuller boxCuller) {
		super(playerView, playerProjection, shadowLightVectorFromOrigin, boxCuller);
	}

	@Override
	public boolean isVisible(AABB aabb) {
		if (boxCuller != null && !boxCuller.isCulled(aabb)) {
			return true;
		}

		return this.isVisible(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) != 0;
	}

	@Override
	public int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (boxCuller != null && !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return 2;
		}

		return isVisible(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ) || this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ) > 0;
	}
}
