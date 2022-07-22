package net.coderbot.iris.vertices;

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

import net.coderbot.iris.vendored.joml.Vector3f;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class NormalHelper {
	private NormalHelper() { }

	/**
	 * Stores a normal plus an extra value as a quartet of signed bytes.
	 * This is the same normal format that vanilla item rendering expects.
	 * The extra value is for use by shaders.
	 */
	public static int packNormal(float x, float y, float z, float w) {
		x = Mth.clamp(x, -1, 1);
		y = Mth.clamp(y, -1, 1);
		z = Mth.clamp(z, -1, 1);
		w = Mth.clamp(w, -1, 1);

		return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16) | (((int) (w * 127) & 0xFF) << 24);
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
	 * Version of {@link #packNormal(float, float, float, float)} that accepts a vector type.
	 */
	public static int packNormal(Vector3f normal, float w) {
		return packNormal(normal.x, normal.y, normal.z, w);
	}

	/**
	 * Retrieves values packed by {@link #packNormal(float, float, float, float)}.
	 *
	 * <p>Components are x, y, z, w - zero based.
	 */
	public static float getPackedNormalComponent(int packedNormal, int component) {
		return ((byte) (packedNormal >> (8 * component))) / 127f;
	}

	/**
	 * Computes the face normal of the given quad and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm.
	 * Expects convex quads with all points co-planar.
	 */
	public static void computeFaceNormal(@NotNull Vector3f saveTo, QuadView q) {
//		final Direction nominalFace = q.nominalFace();
//
//		if (GeometryHelper.isQuadParallelToFace(nominalFace, q)) {
//			Vec3i vec = nominalFace.getVector();
//			saveTo.set(vec.getX(), vec.getY(), vec.getZ());
//			return;
//		}

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

		final float dx0 = x2 - x0;
		final float dy0 = y2 - y0;
		final float dz0 = z2 - z0;
		final float dx1 = x3 - x1;
		final float dy1 = y3 - y1;
		final float dz1 = z3 - z1;

		float normX = dy0 * dz1 - dz0 * dy1;
		float normY = dz0 * dx1 - dx0 * dz1;
		float normZ = dx0 * dy1 - dy0 * dx1;

		float l = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);

		if (l != 0) {
			normX /= l;
			normY /= l;
			normZ /= l;
		}

		saveTo.set(normX, normY, normZ);
	}

	/**
	 * Computes the face normal of the given tri and saves it in the provided non-null vector.
	 *
	 * <p>Assumes counter-clockwise winding order, which is the norm.
	 */
	public static void computeFaceNormalTri(@NotNull Vector3f saveTo, TriView t) {
//		final Direction nominalFace = q.nominalFace();
//
//		if (GeometryHelper.isQuadParallelToFace(nominalFace, q)) {
//			Vec3i vec = nominalFace.getVector();
//			saveTo.set(vec.getX(), vec.getY(), vec.getZ());
//			return;
//		}

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
		final float dx0 = x2 - x0;
		final float dy0 = y2 - y0;
		final float dz0 = z2 - z0;
		final float dx1 = x0 - x1;
		final float dy1 = y0 - y1;
		final float dz1 = z0 - z1;

		float normX = dy0 * dz1 - dz0 * dy1;
		float normY = dz0 * dx1 - dx0 * dz1;
		float normZ = dx0 * dy1 - dy0 * dx1;

		float l = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);

		if (l != 0) {
			normX /= l;
			normY /= l;
			normZ /= l;
		}

		saveTo.set(normX, normY, normZ);
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

		return packNormal(tangentx, tangenty, tangentz, tangentW);
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
}
