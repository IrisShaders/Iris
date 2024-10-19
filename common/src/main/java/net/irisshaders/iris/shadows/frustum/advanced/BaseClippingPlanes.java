package net.irisshaders.iris.shadows.frustum.advanced;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class BaseClippingPlanes {
	private final Vector4f[] planes = new Vector4f[6];

	public BaseClippingPlanes(Matrix4fc modelViewProjection) {
		this.init(modelViewProjection);
	}

	private static Vector4f transform(Matrix4fc transform, float x, float y, float z) {
		Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
		vector4f.mul(transform);
		vector4f.normalize();

		return vector4f;
	}

	private void init(Matrix4fc modelViewProjection) {
		// Transform = Transpose(Projection x View)

		Matrix4f transform = new Matrix4f(modelViewProjection);
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
