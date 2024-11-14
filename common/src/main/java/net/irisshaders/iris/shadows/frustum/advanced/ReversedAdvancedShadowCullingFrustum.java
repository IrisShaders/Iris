package net.irisshaders.iris.shadows.frustum.advanced;

import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

// TODO: I don't know what this class does and I probably broke it by changing AdvancedShadowCullingFrustum
public class ReversedAdvancedShadowCullingFrustum extends AdvancedShadowCullingFrustum {
	private final BoxCuller distanceCuller;

	public ReversedAdvancedShadowCullingFrustum(Matrix4fc modelViewProjection, Matrix4fc shadowProjection, Vector3f shadowLightVectorFromOrigin, BoxCuller voxelCuller, BoxCuller distanceCuller) {
		super(modelViewProjection, shadowProjection, shadowLightVectorFromOrigin, voxelCuller);
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

		// TODO: ?
		return this.isVisible(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) != FrustumIntersection.OUTSIDE;
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (distanceCuller != null && distanceCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		}

		if (boxCuller != null && !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
			return true;
		}

		// TODO: ?
		return this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ) != FrustumIntersection.OUTSIDE;
	}

	@Override
	public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// TODO: placeholder
		return FrustumIntersection.INSIDE;
	}
}
