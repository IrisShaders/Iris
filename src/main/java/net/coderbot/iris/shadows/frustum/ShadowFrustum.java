package net.coderbot.iris.shadows.frustum;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class ShadowFrustum extends Frustum {
	private final BoxCuller boxCuller;

	public ShadowFrustum(Matrix4f view, Matrix4f projection, BoxCuller boxCuller) {
		super(view, projection);

		this.boxCuller = boxCuller;
	}

	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
		super.prepare(cameraX, cameraY, cameraZ);

		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// for Sodium
	// TODO: Better way to do this... Maybe we shouldn't be using a frustum for the box culling in the first place!
	public boolean preAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		if (boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isVisible(AABB aabb) {
		if (boxCuller.isCulled(aabb)) {
			return false;
		}

		return super.isVisible(aabb);
	}
}
