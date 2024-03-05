package net.irisshaders.iris.shadows.frustum.advanced;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class BaseClippingPlanes {
	private final Vector4f[] planes = new Vector4f[6];

	public BaseClippingPlanes(Matrix4f playerView, Matrix4f playerProjection) {
		this.init(playerView, playerProjection);
	}

	private static Vector4f transform(Matrix4f transform, float x, float y, float z) {
		Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
		vector4f.mul(transform);
		vector4f.normalize();

		return vector4f;
	}

	private void init(Matrix4f view, Matrix4f projection) {
		// Transform = Transpose(Projection x View)

		Matrix4f transform = new Matrix4f(projection);
		transform.mul(view);
		transform.transpose();

		planes[0] = transform(transform, -1, 0, 0);
		planes[1] = transform(transform, 1, 0, 0);
		planes[2] = transform(transform, 0, -1, 0);
		planes[3] = transform(transform, 0, 1, 0);
		// FAR clipping plane
		planes[4] = transform(transform, 0, 0, -1);
		// NEAR clipping plane
		planes[5] = transform(transform, 0, 0, 1);
	}

	public Vector4f[] getPlanes() {
		return planes;
	}
}
