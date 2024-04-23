package net.irisshaders.iris.shadows.frustum.advanced;

import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ReversedAdvancedShadowCullingFrustum extends AdvancedShadowCullingFrustum {
	private final BoxCuller distanceCuller;

	public ReversedAdvancedShadowCullingFrustum(Matrix4f playerView, Matrix4f playerProjection, Vector3f shadowLightVectorFromOrigin, BoxCuller voxelCuller, BoxCuller distanceCuller) {
		super(playerView, playerProjection, shadowLightVectorFromOrigin, voxelCuller);
		this.distanceCuller = distanceCuller;
	}

	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
		if (this.distanceCuller != null) {
			this.distanceCuller.setPosition(cameraX, cameraY, cameraZ);
		}
		super.prepare(cameraX, cameraY, cameraZ);
	}

	@Override
	public boolean isVisible(AABB aabb) {
		if (distanceCuller != null && distanceCuller.isCulled(aabb)) {
			return false;
		}

		if (boxCuller != null && !boxCuller.isCulled(aabb)) {
			return true;
		}

		return this.isVisible(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) != 0;
	}

	@Override
	public int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (distanceCuller != null && distanceCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return 0;
		}

		if (boxCuller != null && !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return 2;
		}

		return isVisible(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (distanceCuller != null && distanceCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		}

		if (boxCuller != null && !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return true;
		}

		return this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ) > 0;
	}
}
