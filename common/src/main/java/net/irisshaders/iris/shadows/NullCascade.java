package net.irisshaders.iris.shadows;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static net.irisshaders.iris.shadows.ShadowRenderTargets.NUM_CASCADES;

public class NullCascade {
	private static final float[] tile_dist = new float[]{5, 12, 30, 80};

	private static final float SHADOW_CSM_FIT_FARSCALE = 1.1f;
	private static final float SHADOW_CSM_FITSCALE = 0.1f;
	private static final Vector3f[] frustum = new Vector3f[]{
		new Vector3f(-1.0f, -1.0f, -1.0f),
		new Vector3f(1.0f, -1.0f, -1.0f),
		new Vector3f(-1.0f, 1.0f, -1.0f),
		new Vector3f(1.0f, 1.0f, -1.0f),
		new Vector3f(-1.0f, -1.0f, 1.0f),
		new Vector3f(1.0f, -1.0f, 1.0f),
		new Vector3f(-1.0f, 1.0f, 1.0f),
		new Vector3f(1.0f, 1.0f, 1.0f)};

	static float GetCascadeDistance(float shadowDistance, float far, final int tile) {
		float maxDist = Math.min(shadowDistance, far * SHADOW_CSM_FIT_FARSCALE);

		if (tile == 2) {
			return tile_dist[2] + Math.max(maxDist - tile_dist[2], 0.0f) * SHADOW_CSM_FITSCALE;
		} else if (tile == 3) {
			return maxDist;
		}

		return tile_dist[tile];
	}

	private static void SetProjectionRange(Matrix4f projection, float near, float far) {
		float scale = far / (far - near);
		projection.m22(scale);
		projection.m32(-scale * near);
	}

	public static void GetFrustumMinMax(Matrix4f matProjection, Vector3f clipMin, Vector3f clipMax) {

		Vector4f temp = new Vector4f();
		Vector3f shadowClipPos = new Vector3f();

		for (int i = 0; i < 8; i++) {
			matProjection.transform(new Vector4f(frustum[i], 1.0f), temp);
			unproject(temp, shadowClipPos);

			if (i == 0) {
				clipMin.set(shadowClipPos);
				clipMax.set(shadowClipPos);
			} else {
				clipMin.min(shadowClipPos);
				clipMax.max(shadowClipPos);
			}
		}
	}

	private static void unproject(Vector4f clipPos, Vector3f result) {
		result.x = clipPos.x / clipPos.w;
		result.y = clipPos.y / clipPos.w;
		result.z = clipPos.z / clipPos.w;
	}

	public static Matrix4f GetShadowTileProjectionMatrix(float shadowDistance, float far, float near, Matrix4f shadowModelView, float[] cascadeSizes, int tile, Vector2f shadowViewMin, Vector2f shadowViewMax) {
		float tileSize = cascadeSizes[tile];
		float projectionSize = tileSize * 2.0f + 3.0f;
		float zNear = -far;
		float zFar = far * 2.0f;

		Matrix4f matShadowProjection = ShadowMatrices.createOrthoMatrix(projectionSize, zNear, zFar);

		Matrix4f matSceneProjectionRanged = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
		Matrix4f matSceneModelView = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());

		float rangeNear = tile > 0 ? GetCascadeDistance(shadowDistance, far,tile - 1) : near;
		rangeNear = Math.max(rangeNear - 3.0f, near);
		float rangeFar = tileSize + 3.0f;

		SetProjectionRange(matSceneProjectionRanged, rangeNear, rangeFar);

		Matrix4f matModelViewProjectionInv = new Matrix4f();
		matSceneProjectionRanged.mul(matSceneModelView, matModelViewProjectionInv).invert();

		Matrix4f matSceneToShadow = new Matrix4f();
		shadowModelView.mul(matModelViewProjectionInv, matSceneToShadow);
		matShadowProjection.mul(matSceneToShadow, matSceneToShadow);

		Vector3f clipMin = new Vector3f();
		Vector3f clipMax = new Vector3f();
		GetFrustumMinMax(matSceneToShadow, clipMin, clipMax);

		clipMin.max(new Vector3f(-1.0f));
		clipMax.min(new Vector3f(1.0f));

		float viewScale = 2.0f / projectionSize;
		shadowViewMin.set(clipMin.x / viewScale, clipMin.y / viewScale);
		shadowViewMax.set(clipMax.x / viewScale, clipMax.y / viewScale);

		Vector2f blockPadding = new Vector2f(
			3.0f * matShadowProjection.m00(),
			3.0f * matShadowProjection.m11()
		);

		clipMin.sub(blockPadding.x, blockPadding.y, 0.0f);
		clipMax.add(blockPadding.x, blockPadding.y, 0.0f);

		clipMin.max(new Vector3f(-1.0f));
		clipMax.min(new Vector3f(1.0f));

		Vector2f center = new Vector2f(clipMin.x + clipMax.x, clipMin.y + clipMax.y);
		Vector2f scale = new Vector2f(2.0f).div(clipMax.x - clipMin.x, clipMax.y - clipMin.y);

		Matrix4f matProjScale = new Matrix4f().scaling(scale.x, scale.y, 1.0f);
		Matrix4f matProjTranslate = new Matrix4f().translation(-center.x, -center.y, 0.0f);

		return matProjScale.mul(matProjTranslate.mul(matShadowProjection, new Matrix4f()));
	}

	// tile: 0-3
	private static Vector2f GetShadowTilePos(int tile) {
		if (tile < 0) return new Vector2f(10.0f);

		Vector2f pos = new Vector2f();
		pos.x = fract(tile / 2.0f);
		pos.y = (float) (Math.floor((float) tile * 0.5f) * 0.5f);
		return pos;
	}

	private static float fract(float v) {
		return (float) (v - Math.floor(v));
	}

	public static CascadeOutput getCascades(Matrix4f modelView, float near, float far, float distance) {
		CascadeOutput out = new CascadeOutput();

		float[] cascadeSizes = new float[4];
		cascadeSizes[0] = GetCascadeDistance(distance, far, 0);
		cascadeSizes[1] = GetCascadeDistance(distance, far, 1);
		cascadeSizes[2] = GetCascadeDistance(distance, far, 2);
		cascadeSizes[3] = GetCascadeDistance(distance, far, 3);

		for (int i = 0; i < NUM_CASCADES; i++) {
			out.cascadeSize[i] = cascadeSizes[i];
			out.shadowProjectionPos[i] = GetShadowTilePos(i);
			out.cascadeProjection[i] = GetShadowTileProjectionMatrix(distance, far, near, modelView, cascadeSizes, i, out.cascadeViewMin[i], out.cascadeViewMax[i]);

			out.shadowProjectionSize[i] = new Vector2f(2.0f).div(new Vector2f(
				out.cascadeProjection[i].m00(),
				out.cascadeProjection[i].m11()));
		}

		return out;
	}

	Vector3f GetCascadePaddedFrustumClipBounds(final Matrix4f matShadowProjection, final float padding) {
		return new Vector3f(1.0f + padding).mul(new Vector3f(
			matShadowProjection.m00(),
			matShadowProjection.m11(),
			-matShadowProjection.m22()));
	}

	public static class CascadeOutput {
		public Matrix4f[] cascadeProjection = new Matrix4f[NUM_CASCADES];
		public float[] cascadeSize = new float[NUM_CASCADES];
		public Vector2f[] shadowProjectionSize = new Vector2f[NUM_CASCADES];
		public Vector2f[] shadowProjectionPos = new Vector2f[NUM_CASCADES];
		public Vector2f[] cascadeViewMin = new Vector2f[NUM_CASCADES];
		public Vector2f[] cascadeViewMax = new Vector2f[NUM_CASCADES];

		public CascadeOutput() {
			for (int i = 0; i < NUM_CASCADES; i++) {
				cascadeProjection[i] = new Matrix4f();
				cascadeViewMin[i] = new Vector2f();
				cascadeViewMax[i] = new Vector2f();
				shadowProjectionSize[i] = new Vector2f();
				shadowProjectionPos[i] = new Vector2f();
			}
		}
	}
}
