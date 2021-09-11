package net.coderbot.iris.shadows.frustum;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class ShadowFrustum extends Frustum {
	private final BoxCuller boxCuller;

	public ShadowFrustum(Matrix4f view, Matrix4f projection, BoxCuller boxCuller) {
		super(view, projection);

		this.boxCuller = boxCuller;
	}

	public void setPosition(double cameraX, double cameraY, double cameraZ) {
		super.setPosition(cameraX, cameraY, cameraZ);

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

	public boolean isVisible(Box box) {
		if (boxCuller.isCulled(box)) {
			return false;
		}

		return super.isVisible(box);
	}
}
