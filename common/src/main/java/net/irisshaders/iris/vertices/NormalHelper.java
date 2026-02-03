package net.irisshaders.iris.vertices;

/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.irisshaders.iris.vertices.views.QuadView;
import net.irisshaders.iris.vertices.views.TriView;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public abstract class NormalHelper {
	private static final float EPS = 1e-20f;

	private NormalHelper() {
	}

	public static int invertPackedNormal(int packed) {
		int ix = -(packed & 255);
		int iy = -((packed >> 8) & 255);
		int iz = -((packed >> 16) & 255);

		ix &= 255;
		iy &= 255;
		iz &= 255;

		return (packed & 0xFF000000) | (iz << 16) | (iy << 8) | ix;
	}

	/**
	 * Computes the face normal of the given quad and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm.
	 * Expects convex quads with all points co-planar.
	 */
	public static void computeFaceNormal(@NotNull Vector3f saveTo, QuadView q) {
		final float x0 = q.x(0);
		final float y0 = q.y(0);
		final float z0 = q.z(0);
		final float x1 = q.x(1);
		final float y1 = q.y(1);
		final float z1 = q.z(1);
		final float x2 = q.x(2);
		final float y2 = q.y(2);
		final float z2 = q.z(2);
		final float x3 = q.x(3);
		final float y3 = q.y(3);
		final float z3 = q.z(3);

		computeFaceNormalManual(saveTo, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3);
	}

	/**
	 * Computes the face normal of the given quad and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm.
	 * Expects convex quads with all points co-planar.
	 */
	public static void computeFaceNormalManual(@NotNull Vector3f saveTo,
											   float x0, float y0, float z0,
											   float x1, float y1, float z1,
											   float x2, float y2, float z2,
											   float x3, float y3, float z3) {
		final float dx0 = x2 - x0;
		final float dy0 = y2 - y0;
		final float dz0 = z2 - z0;
		final float dx1 = x3 - x1;
		final float dy1 = y3 - y1;
		final float dz1 = z3 - z1;

		float normX = dy0 * dz1 - dz0 * dy1;
		float normY = dz0 * dx1 - dx0 * dz1;
		float normZ = dx0 * dy1 - dy0 * dx1;

		saveTo.set(normX, normY, normZ);

		saveTo.normalize();
	}

	/**
	 * Computes the face normal of the given quad with a flipped order and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm. It will be read clockwise to flip it.
	 * Expects convex quads with all points co-planar.
	 */
	public static void computeFaceNormalFlipped(@NotNull Vector3f saveTo, QuadView q) {
		final float x0 = q.x(3);
		final float y0 = q.y(3);
		final float z0 = q.z(3);
		final float x1 = q.x(2);
		final float y1 = q.y(2);
		final float z1 = q.z(2);
		final float x2 = q.x(1);
		final float y2 = q.y(1);
		final float z2 = q.z(1);
		final float x3 = q.x(0);
		final float y3 = q.y(0);
		final float z3 = q.z(0);

		computeFaceNormalManual(saveTo, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3);
	}

	/**
	 * Computes the face normal of the given tri and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm.
	 */
	public static void computeFaceNormalTri(@NotNull Vector3f saveTo, TriView t) {
		final float x0 = t.x(0);
		final float y0 = t.y(0);
		final float z0 = t.z(0);
		final float x1 = t.x(1);
		final float y1 = t.y(1);
		final float z1 = t.z(1);
		final float x2 = t.x(2);
		final float y2 = t.y(2);
		final float z2 = t.z(2);

		// note: subtraction order is significant here because of how the cross product works.
		// If we're wrong our calculated normal will be pointing in the opposite direction of how it should.
		// This current order is similar enough to the order in the quad variant.
		computeFaceNormalManual(saveTo, x0, y0, z0, x1, y1, z1, x2, y2, z2, x0, y0, z0);
	}

	public static int computeTangentSmooth(float normalX, float normalY, float normalZ, TriView t) {
		// Capture all of the relevant vertex positions
		float x0 = t.x(0);
		float y0 = t.y(0);
		float z0 = t.z(0);

		float x1 = t.x(1);
		float y1 = t.y(1);
		float z1 = t.z(1);

		float x2 = t.x(2);
		float y2 = t.y(2);
		float z2 = t.z(2);

		// Project all vertices onto normal plane (for smooth normal support). Optionally skip this step for flat shading.
		// Procedure:
		// project v onto normal
		// offset v by the projection to get the point on the plane
		// project x0, y0, z0 onto normal
		float d0 = x0 * normalX + y0 * normalY + z0 * normalZ;
		float d1 = x1 * normalX + y1 * normalY + z1 * normalZ;
		float d2 = x2 * normalX + y2 * normalY + z2 * normalZ;

		// offset x, y, z by the projection to get the projected point on the normal plane
		x0 -= d0 * normalX;
		y0 -= d0 * normalY;
		z0 -= d0 * normalZ;

		x1 -= d1 * normalX;
		y1 -= d1 * normalY;
		z1 -= d1 * normalZ;

		x2 -= d2 * normalX;
		y2 -= d2 * normalY;
		z2 -= d2 * normalZ;


		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float u0 = t.u(0);
		float v0 = t.v(0);

		float u1 = t.u(1);
		float v1 = t.v(1);

		float u2 = t.u(2);
		float v2 = t.v(2);

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		// Be very careful when writing out complex multi-step calculations
		// such as vector cross products! The calculation for pbitangentz
		// used to be broken because it multiplied values in the wrong order.

		float pbitangentx = tangenty * normalZ - tangentz * normalY;
		float pbitangenty = tangentz * normalX - tangentx * normalZ;
		float pbitangentz = tangentx * normalY - tangenty * normalX;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		return NormI8.pack(tangentx, tangenty, tangentz, tangentW);
	}

	public static int computeTangent(float normalX, float normalY, float normalZ, TriView t) {
		// Capture all of the relevant vertex positions
		float x0 = t.x(0);
		float y0 = t.y(0);
		float z0 = t.z(0);

		float x1 = t.x(1);
		float y1 = t.y(1);
		float z1 = t.z(1);

		float x2 = t.x(2);
		float y2 = t.y(2);
		float z2 = t.z(2);

		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float u0 = t.u(0);
		float v0 = t.v(0);

		float u1 = t.u(1);
		float v1 = t.v(1);

		float u2 = t.u(2);
		float v2 = t.v(2);

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		// Be very careful when writing out complex multi-step calculations
		// such as vector cross products! The calculation for pbitangentz
		// used to be broken because it multiplied values in the wrong order.

		float pbitangentx = tangenty * normalZ - tangentz * normalY;
		float pbitangenty = tangentz * normalX - tangentx * normalZ;
		float pbitangentz = tangentx * normalY - tangenty * normalX;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		return NormI8.pack(tangentx, tangenty, tangentz, tangentW);
	}

	public static int computeTangent(float normalX, float normalY, float normalZ, float x0, float y0, float z0, float u0, float v0,
									 float x1, float y1, float z1, float u1, float v1,
									 float x2, float y2, float z2, float u2, float v2) {
		return computeTangent(null, normalX, normalY, normalZ, x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2);
	}

	public static int computeTangent(Vector4f output, float normalX, float normalY, float normalZ, float x0, float y0, float z0, float u0, float v0,
									 float x1, float y1, float z1, float u1, float v1,
									 float x2, float y2, float z2, float u2, float v2) {
		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		if (tangentx == 0.0f && tangenty == 0.0f && tangentz == 0.0f) {
			return -1;
		}

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		// Be very careful when writing out complex multi-step calculations
		// such as vector cross products! The calculation for pbitangentz
		// used to be broken because it multiplied values in the wrong order.

		float pbitangentx = tangenty * normalZ - tangentz * normalY;
		float pbitangenty = tangentz * normalX - tangentx * normalZ;
		float pbitangentz = tangentx * normalY - tangenty * normalX;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		if (output != null) {
			output.set(tangentx, tangenty, tangentz, tangentW);
		}

		return NormI8.pack(tangentx, tangenty, tangentz, tangentW);
	}

	private static float rsqrt(float value) {
		if (value == 0.0f) {
			// You heard it here first, folks: 1 divided by 0 equals 1
			// In actuality, this is a workaround for normalizing a zero length vector (leaving it as zero length)
			return 1.0f;
		} else {
			return (float) (1.0 / Math.sqrt(value));
		}
	}

	private static int snorm12(float v) {
		float c = Math.max(-1.0f, Math.min(1.0f, v));
		int q = Math.round(c * 2047.0f);
		if (q == -2048) q = -2047;
		return q;
	}

	private static float signNotZero(float v) {
		return (v >= 0.0f) ? +1.0f : -1.0f;
	}

	public static int encodeNormal(float x, float y, float z) {
        /*
        Original GLSL from the paper:

        // Project the sphere onto the octahedron, and then onto the xy plane
        vec2 p = v.xy * (1.0 / (abs(v.x) + abs(v.y) + abs(v.z)));
        // Reflect the folds of the lower hemisphere over the diagonals
        return (v.z <= 0.0) ? ((1.0 - abs(p.yx)) * signNotZero(p)) : p;
         */

		float rev = 1.0f / (Math.abs(x) + Math.abs(y) + Math.abs(z));
		float pX = x * rev;
		float pY = y * rev;

		float outX, outY;

		if (z > 0.0f) {
			outX = pX;
			outY = pY;
		} else {
			outX = ((1.0f - Math.abs(pY)) * signNotZero(pX));
			outY = ((1.0f - Math.abs(pX)) * signNotZero(pY));
		}

		int qx = snorm12(outX);
		int qy = snorm12(outY);
		return ((qx & 0xFFF) << 12) | (qy & 0xFFF);
	}

	// https://backend.orbit.dtu.dk/ws/portalfiles/portal/126824972/onb_frisvad_jgt2012_v2.pdf
	private static void onbFromUnitNormal(float nx, float ny, float nz, Vector3f t1, Vector3f t2) {
		float s = nz >= 0.0f ? 1.0f : -1.0f;
		float a = -1.0f / (s + nz);
		float b = nx * ny * a;

		t1.set(1.0f + s * nx * nx * a, s * b, -s * nx).normalize();
		t2.set(
			ny * t1.z - nz * t1.y,
			nz * t1.x - nx * t1.z,
			nx * t1.y - ny * t1.x
		);
	}

	private static float encodeDiamond(float px, float py) {
		float denom = Math.abs(px) + Math.abs(py);
		if (denom <= EPS) return 0.5f;
		float x = px / denom;
		float pys = (py >= 0.0f) ? 1.0f : -1.0f;
		return -pys * 0.25f * x + 0.5f + pys * 0.25f;
	}

	public static int packDiamondByte(Vector3fc normal, Vector3fc tangent,
									  Vector3f t1, Vector3f t2, Vector3f tp) {
		t2.set(normal).normalize();
		float nx = t2.x, ny = t2.y, nz = t2.z;

		onbFromUnitNormal(nx, ny, nz, t1, t2); // This is what fixes problems with UV???

		float NdT = nx * tangent.x() + ny * tangent.y() + nz * tangent.z();
		tp.set(tangent).sub(nx * NdT, ny * NdT, nz * NdT);

		if (tp.lengthSquared() > EPS) {
			tp.normalize();
		} else {
			tp.set(t1);
		}

		float px = tp.dot(t1);
		float py = tp.dot(t2);
		float d = encodeDiamond(px, py);
		int q = Math.min(256, Math.max(0, Math.round(d * 256.0f)));
		return q & 0xFF;
	}

	public static int encodeNormalTangent(Vector3f normal, Vector3f tangent,
										  Vector3f scratch1, Vector3f scratch2, Vector3f scratchOut) {
		int encodedNormal = encodeNormal(normal.x, normal.y, normal.z);
		int encodedTangent = packDiamondByte(normal, tangent, scratch1, scratch2, scratchOut);

		// Bits 0-23: normal (12-bit x, 12-bit y)
		// Bits 24-31: tangent
		return (encodedTangent << 24) | encodedNormal;
	}

}
