package net.coderbot.iris.shadows.frustum;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class ShadowFrustum extends Frustum {
	private final double maxDistance;

	private double minAllowedX;
	private double maxAllowedX;
	private double minAllowedY;
	private double maxAllowedY;
	private double minAllowedZ;
	private double maxAllowedZ;

	public ShadowFrustum(Matrix4f matrix4f, Matrix4f matrix4f2, double maxDistance) {
		super(matrix4f, matrix4f2);

		this.maxDistance = maxDistance;
	}

	public void setPosition(double cameraX, double cameraY, double cameraZ) {
		super.setPosition(cameraX, cameraY, cameraZ);

		this.minAllowedX = cameraX - maxDistance;
		this.maxAllowedX = cameraX + maxDistance;
		this.minAllowedY = cameraY - maxDistance;
		this.maxAllowedY = cameraY + maxDistance;
		this.minAllowedZ = cameraZ - maxDistance;
		this.maxAllowedZ = cameraZ + maxDistance;
	}

	public boolean isVisible(Box box) {
		if (box.maxX < this.minAllowedX || box.minX > this.maxAllowedX) {
			return false;
		}

		if (box.maxY < this.minAllowedY || box.minY > this.maxAllowedY) {
			return false;
		}

		if (box.maxZ < this.minAllowedZ || box.minZ > this.maxAllowedZ) {
			return false;
		}

		return super.isVisible(box);
	}
}
